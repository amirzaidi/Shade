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

    private final Map<NotificationList.Notif, Integer> mNotifs;

    public NotificationRanker(Map<NotificationList.Notif, Integer> notifs) {
        mNotifs = notifs;
    }

    public RankedNotification getBestNotification() {
        int bestPriority = PRIORITY_AT_LEAST;
        long bestPostTime = 0;
        StatusBarNotification bestNotif = null;
        for (Map.Entry<NotificationList.Notif, Integer> kvp : mNotifs.entrySet()) {
            StatusBarNotification sbn = kvp.getKey().getSbn();
            if (shouldBeFilteredOut(sbn)) {
                continue;
            }

            Notification n = sbn.getNotification();
            if (TextUtils.isEmpty(n.extras.getCharSequence(Notification.EXTRA_TITLE))
                    || TextUtils.isEmpty(n.extras.getCharSequence(Notification.EXTRA_TEXT))) {
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
