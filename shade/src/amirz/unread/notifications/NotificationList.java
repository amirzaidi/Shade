package amirz.unread.notifications;

import android.app.Notification;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.android.launcher3.Utilities;
import com.android.launcher3.notification.NotificationKeyData;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.util.PackageUserKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationList
        implements NotificationListener.NotificationsChangedListener {
    private final HideTracker mHideTracker;
    private final Handler mWorkerHandler;
    private final Runnable mOnNotificationsChanged;

    private final Set<NotificationKeyData> mSbn = new HashSet<>();
    private final NotificationListenerService.Ranking mTempRanking
            = new NotificationListenerService.Ranking();

    public NotificationList(HideTracker hideTracker, Handler workerHandler, Runnable onChange) {
        mHideTracker = hideTracker;
        mWorkerHandler = workerHandler;
        mOnNotificationsChanged = onChange;
    }

    public Map<StatusBarNotification, Integer> getMap() {
        return getMap(Integer.MIN_VALUE, true);
    }

    public Map<StatusBarNotification, Integer> getMap(int minPriority) {
        return getMap(minPriority, false);
    }

    // Warning: Call getMap only from the worker thread, to prevent modification to mSbn.
    private Map<StatusBarNotification, Integer> getMap(int minPriority, boolean all) {
        NotificationListener nls = NotificationListener.getInstanceIfConnected();
        if (nls == null) {
            return Collections.emptyMap();
        }

        NotificationListenerService.RankingMap map = nls.getCurrentRanking();

        Map<NotificationKeyData, Integer> rankedKeys = new HashMap<>();
        for (NotificationKeyData n : mSbn) {
            if (map.getRanking(n.notificationKey, mTempRanking)) {
                int priority = getRankedImportance();
                if (priority >= minPriority) {
                    rankedKeys.put(n, priority);
                }
            }
        }

        Map<StatusBarNotification, Integer> rankedSbn = new HashMap<>();
        List<StatusBarNotification> sbnList
                = nls.getNotificationsForKeys(new ArrayList<>(rankedKeys.keySet()));

        for (StatusBarNotification sbn : sbnList) {
            if (map.getRanking(sbn.getKey(), mTempRanking) && (all || !shouldBeFilteredOut(sbn))) {
                rankedSbn.put(sbn, getRankedImportance());
            }
        }
        return rankedSbn;
    }

    private boolean shouldBeFilteredOut(StatusBarNotification sbn) {
        if (!sbn.isClearable()) {
            return true;
        }

        Notification notification = sbn.getNotification();
        CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        boolean missingTitleAndText = TextUtils.isEmpty(title) && TextUtils.isEmpty(text);
        boolean isGroupHeader = (notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0;
        return isGroupHeader || missingTitleAndText
                || mHideTracker.allActivitiesHidden(sbn.getPackageName());
    }

    @Override
    public void onNotificationPosted(PackageUserKey postedPackageUserKey,
                                     NotificationKeyData notificationKey,
                                     boolean shouldBeFilteredOut) {
        mWorkerHandler.post(() -> {
            mSbn.add(notificationKey);
            mOnNotificationsChanged.run();
        });
    }

    @Override
    public void onNotificationRemoved(PackageUserKey removedPackageUserKey,
                                      NotificationKeyData notificationKey) {
        mWorkerHandler.post(() -> {
            mSbn.remove(notificationKey);
            mOnNotificationsChanged.run();
        });
    }

    @Override
    public void onNotificationFullRefresh(List<StatusBarNotification> activeNotifications) {
        // Ignore filtered notifications, and instead load all notifications.
        // This is called whenever the service is bound.
        mWorkerHandler.post(() -> {
            List<NotificationKeyData> newSbn = new ArrayList<>();
            NotificationListener nls = NotificationListener.getInstanceIfConnected();
            if (nls != null) {
                try {
                    for (StatusBarNotification sbn : nls.getActiveNotifications()) {
                        newSbn.add(NotificationKeyData.fromNotification(sbn));
                    }
                } catch (SecurityException ignored) {
                    // Can throw an exception when loading another user profile.
                }
            }
            mSbn.clear();
            mSbn.addAll(newSbn);
            mOnNotificationsChanged.run();
        });
    }

    private int getRankedImportance() {
        return Utilities.ATLEAST_OREO
                ? mTempRanking.getChannel().getImportance()
                : mTempRanking.getImportance();
    }
}
