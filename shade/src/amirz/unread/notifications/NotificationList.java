package amirz.unread.notifications;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

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
        implements NotificationListener.StatusBarNotificationsChangedListener,
        NotificationListener.NotificationsChangedListener {
    public static class Notif {
        private final StatusBarNotification mSbn;

        private Notif(StatusBarNotification sbn) {
            mSbn = sbn;
        }

        public StatusBarNotification getSbn() {
            return mSbn;
        }

        @Override
        public int hashCode() {
            return mSbn.getKey().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Notif) {
                return ((Notif) obj).mSbn.getKey().equals(mSbn.getKey());
            }
            return super.equals(obj);
        }
    }

    private final Map<Notif, Integer> mSbn = new HashMap<>();
    private final NotificationListenerService.Ranking mTempRanking
            = new NotificationListenerService.Ranking();
    private final Runnable mOnNotificationsChanged;

    public NotificationList(Runnable onNotificationsChanged) {
        mOnNotificationsChanged = onNotificationsChanged;
    }

    public Map<Notif, Integer> getMap() {
        return Collections.unmodifiableMap(mSbn);
    }

    public Set<Notif> getSbn() {
        return Collections.unmodifiableSet(mSbn.keySet());
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        NotificationListener nls = NotificationListener.getInstanceIfConnected();
        if (nls != null) {
            NotificationListenerService.RankingMap map = nls.getCurrentRanking();
            if (map.getRanking(sbn.getKey(), mTempRanking)) {
                mSbn.put(new Notif(sbn), getRankedImportance());
                mOnNotificationsChanged.run();
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (mSbn.remove(new Notif(sbn)) != null) {
            mOnNotificationsChanged.run();
        }
    }

    @Override
    public void onNotificationPosted(PackageUserKey postedPackageUserKey,
                                     NotificationKeyData notificationKey,
                                     boolean shouldBeFilteredOut) {
        // No-op
    }

    @Override
    public void onNotificationRemoved(PackageUserKey removedPackageUserKey,
                                      NotificationKeyData notificationKey) {
        // No-op
    }

    @Override
    public void onNotificationFullRefresh(List<StatusBarNotification> activeNotifications) {
        // Ignore filtered notifications, and instead load all notifications.
        // This is called whenever the service is bound.
        if (reloadNotifications()) {
            mOnNotificationsChanged.run();
        }
    }

    public boolean reloadNotifications() {
        NotificationListener nls = NotificationListener.getInstanceIfConnected();
        boolean sbnChanged = false;
        if (nls == null) {
            sbnChanged = !mSbn.isEmpty();
            mSbn.clear();
        } else {
            List<Notif> notifList = new ArrayList<>();
            for (StatusBarNotification sbn : nls.getActiveNotifications()) {
                notifList.add(new Notif(sbn));
            }

            for (Notif n : new HashSet<>(mSbn.keySet())) {
                if (!notifList.contains(n)) {
                    sbnChanged = true;
                    mSbn.remove(n);
                }
            }

            NotificationListenerService.RankingMap map = nls.getCurrentRanking();
            for (Notif n : notifList) {
                if (!mSbn.containsKey(n) && map.getRanking(n.getSbn().getKey(), mTempRanking)) {
                    sbnChanged = true;
                    mSbn.put(n, getRankedImportance());
                }
            }
        }
        return sbnChanged;
    }

    private int getRankedImportance() {
        return Utilities.ATLEAST_OREO
                ? mTempRanking.getChannel().getImportance()
                : mTempRanking.getImportance();
    }
}
