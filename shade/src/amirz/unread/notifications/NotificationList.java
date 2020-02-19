package amirz.unread.notifications;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.android.launcher3.notification.NotificationListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NotificationList implements NotificationListener.StatusBarNotificationsChangedListener {
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
                mSbn.put(new Notif(sbn), mTempRanking.getImportance());
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

    public void reloadNotifications() {
        NotificationListener nls = NotificationListener.getInstanceIfConnected();
        if (nls != null) {
            List<Notif> notifList = new ArrayList<>();
            for (StatusBarNotification sbn : nls.getActiveNotifications()) {
                notifList.add(new Notif(sbn));
            }

            for (Notif n : new HashSet<>(mSbn.keySet())) {
                if (!notifList.contains(n)) {
                    mSbn.remove(n);
                }
            }

            NotificationListenerService.RankingMap map = nls.getCurrentRanking();
            for (Notif n : notifList) {
                if (!mSbn.containsKey(n) && map.getRanking(n.getSbn().getKey(), mTempRanking)) {
                    mSbn.put(n, mTempRanking.getImportance());
                }
            }
        }
    }
}
