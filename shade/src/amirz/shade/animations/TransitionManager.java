package amirz.shade.animations;

import android.app.ActivityOptions;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppTransitionManager;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;

@SuppressWarnings("unused")
public class TransitionManager extends LauncherAppTransitionManager {
    public static final String KEY_FADING_TRANSITION = "pref_transition";

    private int mDefaultAnimations = -1;

    public TransitionManager(Context context) {
    }

    public ActivityOptions getActivityLaunchOptions(Launcher launcher, View v) {
        return isOverrideEnabled(launcher)
                ? ActivityOptions.makeCustomAnimation(
                        launcher, R.anim.enter_app, R.anim.exit_launcher)
                : super.getActivityLaunchOptions(launcher, v);
    }

    public void applyWindowPreference(Launcher launcher) {
        Window window = launcher.getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        if (mDefaultAnimations == -1) {
            // Save the default in case we need to restore it later.
            mDefaultAnimations = attributes.windowAnimations;
        }
        attributes.windowAnimations =
                isOverrideEnabled(launcher)
                        ? R.style.ShadeAnimations
                        : mDefaultAnimations;
        window.setAttributes(attributes);
    }

    private boolean isOverrideEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(KEY_FADING_TRANSITION, true);
    }
}
