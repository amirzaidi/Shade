package amirz.unread.notifications;

import android.app.Notification;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;

import com.android.launcher3.notification.NotificationListener;

import java.util.List;

import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_DEFAULT;
import static androidx.core.app.NotificationManagerCompat.IMPORTANCE_MIN;

public class NotificationRanker {
    private static final int PRIORITY_AT_LEAST = IMPORTANCE_DEFAULT;

    private final List<StatusBarNotification> mSbn;
    private final NotificationListenerService.Ranking mTempRanking
            = new NotificationListenerService.Ranking();

    public NotificationRanker(List<StatusBarNotification> sbn) {
        mSbn = sbn;
    }

    public RankedNotification getBestNotification() {
        NotificationListenerService nls = NotificationListener.getInstanceIfConnected();
        if (nls == null) {
            return null;
        }

        int bestPriority = IMPORTANCE_MIN;
        long bestPostTime = 0;
        StatusBarNotification bestNotif = null;
        for (StatusBarNotification n : mSbn) {
            if (n.isOngoing()) {
                continue;
            }

            Notification data = n.getNotification();
            if (TextUtils.isEmpty(data.extras.getCharSequence(Notification.EXTRA_TITLE))
                    || TextUtils.isEmpty(data.extras.getCharSequence(Notification.EXTRA_TEXT))) {
                continue;
            }

            NotificationListenerService.RankingMap map = nls.getCurrentRanking();
            if (!map.getRanking(n.getKey(), mTempRanking)) {
                continue;
            }
            int priority = mTempRanking.getImportance();

            if (priority > bestPriority
                    || (priority == bestPriority && n.getPostTime() > bestPostTime)) {
                bestPriority = priority;
                bestPostTime = n.getPostTime();
                bestNotif = n;
            }
        }

        return bestNotif == null
                ? null
                : new RankedNotification(bestNotif, bestPriority >= PRIORITY_AT_LEAST);
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
