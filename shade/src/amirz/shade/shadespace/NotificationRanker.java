package amirz.shade.shadespace;

import android.service.notification.StatusBarNotification;

import java.util.List;

import static android.app.Notification.PRIORITY_DEFAULT;
import static android.app.Notification.PRIORITY_MIN;

class NotificationRanker {
    private static final int PRIORITY_AT_LEAST = PRIORITY_DEFAULT;
    private final List<StatusBarNotification> mSbn;

    NotificationRanker(List<StatusBarNotification> sbn) {
        mSbn = sbn;
    }

    RankedNotification getBestNotification() {
        int bestPriority = PRIORITY_MIN;
        StatusBarNotification bestNotif = null;
        for (StatusBarNotification n : mSbn) {
            if (!n.isOngoing()) {
                int priority = n.getNotification().priority;
                if (priority >= bestPriority) {
                    bestPriority = priority;
                    bestNotif = n;
                }
            }
        }
        return bestNotif == null
                ? null
                : new RankedNotification(bestNotif, bestPriority >= PRIORITY_AT_LEAST);
    }

    static class RankedNotification {
        final StatusBarNotification sbn;
        final boolean important;

        private RankedNotification(StatusBarNotification sbn, boolean important) {
            this.sbn = sbn;
            this.important = important;
        }
    }
}
