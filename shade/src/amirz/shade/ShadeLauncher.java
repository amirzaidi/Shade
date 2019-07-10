package amirz.shade;

import android.os.Bundle;
import android.view.WindowManager;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;
import com.android.launcher3.plugin.PluginLauncher;
import com.android.launcher3.plugin.PluginManager;
import com.android.launcher3.plugin.unread.UnreadPluginClient;
import com.android.launcher3.uioverrides.WallpaperColorInfo;

import amirz.shade.transitions.TransitionManager;

import static amirz.shade.ShadeSettings.PREF_TRANSITION;

public class ShadeLauncher extends PluginLauncher {
    private enum State {
        STOPPED,
        RECREATE_DEFERRED,
        KILL_DEFERRED,
        STARTED
    }

    private final ShadeCallbacks mCallbacks;
    private State mState = State.STOPPED;
    private int mDefaultWindowAnimations;

    public ShadeLauncher() {
        mCallbacks = new ShadeCallbacks(this);
        setLauncherCallbacks(mCallbacks);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        FeatureFlags.QSB_ON_FIRST_SCREEN = PluginManager.getInstance(this)
                .hasPluginTypeEnabled(UnreadPluginClient.class);
        super.onCreate(savedInstanceState);

        getWorkspace().stripEmptyScreens();
        mDefaultWindowAnimations = getWindow().getAttributes().windowAnimations;
        ShadeRestarter.cancelRestart(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mState == State.KILL_DEFERRED) {
            ShadeRestarter.initiateRestart(this);
        } else if (mState == State.RECREATE_DEFERRED) {
            super.recreate();
        }
        mState = State.STARTED;

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.windowAnimations =
                Utilities.getPrefs(this).getBoolean(PREF_TRANSITION, true)
                        ? R.style.ShadeAnimations
                        : mDefaultWindowAnimations;
        getWindow().setAttributes(attributes);

        TransitionManager transitions = (TransitionManager) getAppTransitionManager();
        transitions.overrideAppClose(this);
    }

    @Override
    public void recreate() {
        if (mState == State.STARTED) {
            super.recreate();
        } else if (mState != State.KILL_DEFERRED) {
            mState = State.RECREATE_DEFERRED;
        }
    }

    public void kill() {
        if (mState == State.STARTED) {
            ShadeRestarter.initiateRestart(this);
        } else {
            mState = State.KILL_DEFERRED;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mState = State.STOPPED;
    }

    @Override
    protected int getThemeRes(WallpaperColorInfo wallpaperColorInfo) {
        return ShadeSettings.getThemeResAdaptive(this, super.getThemeRes(wallpaperColorInfo),
                !wallpaperColorInfo.isDark() && wallpaperColorInfo.supportsDarkText());
    }
}
