package amirz.unread.notifications;

import android.service.notification.StatusBarNotification;

import java.util.Map;

import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT;
import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_HIGH;

public class NotificationRanker {
    private static final int PRIORITY_AT_LEAST = IMPORTANCE_DEFAULT;
    private static final int PRIORITY_IMPORTANT = IMPORTANCE_HIGH;

    private final NotificationList mNotifs;

    public NotificationRanker(NotificationList notifs) {
        mNotifs = notifs;
    }

    public RankedNotification getBestNotification() {
        int bestPriority = PRIORITY_AT_LEAST;
        long bestPostTime = 0;
        StatusBarNotification bestNotif = null;
        for (Map.Entry<StatusBarNotification, Integer> kvp
                : mNotifs.getMap(PRIORITY_AT_LEAST).entrySet()) {
            StatusBarNotification sbn = kvp.getKey();
            int priority = kvp.getValue();
            if (priority > bestPriority
                    || (priority == bestPriority && sbn.getPostTime() > bestPostTime)) {
                bestPriority = priority;
                bestPostTime = sbn.getPostTime();
                bestNotif = sbn;
            }
        }

        return bestNotif == null
                ? null
                : new RankedNotification(bestNotif, bestPriority >= PRIORITY_IMPORTANT);
    }

    public static class RankedNotification {
        public final StatusBarNotification sbn;
        public final boolean important;

        private RankedNotification(StatusBarNotification sbn, boolean important) {
            this.sbn = sbn;
            this.important = important;
        }
    }
}
