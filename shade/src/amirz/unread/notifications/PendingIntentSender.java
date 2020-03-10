package amirz.unread.notifications;

import android.app.PendingIntent;
import android.os.Bundle;
import android.view.View;

import com.android.launcher3.Launcher;

public class PendingIntentSender {
    // Delay updates to keep the notification showing after pressing it.
    private long mLastClick;

    public long getLastClick() {
        return mLastClick;
    }

    public View.OnClickListener onClickNotification(PendingIntent pi) {
        return v -> {
            if (pi != null) {
                mLastClick = System.currentTimeMillis();
                try {
                    Launcher launcher = Launcher.getLauncher(v.getContext());
                    Bundle b = launcher.getAppTransitionManager()
                            .getActivityLaunchOptions(launcher, v).toBundle();
                    pi.send(null, 0, null, null, null, null, b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
