package amirz.unread.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.service.notification.StatusBarNotification;

import com.android.launcher3.notification.NotificationInfo;

public class ParsedNotification {
    public PendingIntent pi;
    public String pkg;
    public String[] splitTitle;
    public String body;

    public ParsedNotification(Context context, StatusBarNotification sbn) {
        NotificationInfo notif = new NotificationInfo(context, sbn);
        pi = notif.intent;
        pkg = notif.packageUserKey.mPackageName;
        String title = notif.title == null
                ? ""
                : notif.title.toString();
        splitTitle = splitTitle(title);
        body = notif.text == null
                ? ""
                : notif.text.toString().trim().split("\n")[0]; // First line
    }

    private String[] splitTitle(String title) {
        final String[] delimiters = {": ", " - ", " • "};
        for (String del : delimiters) {
            if (title.contains(del)) {
                return title.split(del, 2);
            }
        }
        return new String[] { title };
    }
}
