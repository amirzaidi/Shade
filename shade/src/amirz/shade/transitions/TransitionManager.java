package amirz.shade.transitions;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppTransitionManagerImpl;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.quickstep.QuickstepProcessInitializer;

import static amirz.shade.ShadeSettings.PREF_TRANSITION;

public class TransitionManager extends LauncherAppTransitionManagerImpl {
    public TransitionManager(Context context) {
        super(context);
    }

    public ActivityOptions getActivityLaunchOptions(Launcher launcher, View v) {
        if (QuickstepProcessInitializer.isEnabled()) {
            return super.getActivityLaunchOptions(launcher, v);
        }
        if (Utilities.getPrefs(launcher).getBoolean(PREF_TRANSITION, false)) {
            return ActivityOptions.makeCustomAnimation(launcher,
                    R.anim.enter_app, R.anim.exit_launcher);
        }
        if (Utilities.ATLEAST_MARSHMALLOW) {
            int left = 0, top = 0;
            int width = v.getWidth(), height = v.getHeight();
            if (v instanceof BubbleTextView) {
                // Launch from center of icon, not entire view
                Drawable icon = ((BubbleTextView) v).getIcon();
                if (icon != null) {
                    Rect bounds = icon.getBounds();
                    left = (width - bounds.width()) / 2;
                    top = v.getPaddingTop();
                    width = bounds.width();
                    height = bounds.height();
                }
            }
            return ActivityOptions.makeClipRevealAnimation(v, left, top, width, height);
        } else if (Utilities.ATLEAST_LOLLIPOP_MR1) {
            // On L devices, we use the device default slide-up transition.
            // On L MR1 devices, we use a custom version of the slide-up transition which
            // doesn't have the delay present in the device default.
            return ActivityOptions.makeCustomAnimation(launcher, R.anim.task_open_enter,
                    R.anim.no_anim);
        }
        return null;
    }

    public void overrideAppClose(Activity activity) {
        if (!QuickstepProcessInitializer.isEnabled()
                && Utilities.getPrefs(activity).getBoolean(PREF_TRANSITION, false)) {
            activity.overridePendingTransition(R.anim.enter_launcher, R.anim.exit_app);
        }
    }
}
