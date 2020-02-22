package amirz.unread.notifications;

import android.app.Notification;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

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
            if (shouldBeFilteredOut(sbn)) {
                continue;
            }

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

    private boolean shouldBeFilteredOut(StatusBarNotification sbn) {
        if (!sbn.isClearable()) {
            return true;
        }

        Notification notification = sbn.getNotification();
        CharSequence title = notification.extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence text = notification.extras.getCharSequence(Notification.EXTRA_TEXT);
        boolean missingTitleAndText = TextUtils.isEmpty(title) && TextUtils.isEmpty(text);
        boolean isGroupHeader = (notification.flags & Notification.FLAG_GROUP_SUMMARY) != 0;
        return (isGroupHeader || missingTitleAndText);
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
