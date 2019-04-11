package amirz.shade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.ColorUtils;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.AppInfo;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.LauncherState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import amirz.shade.allapps.search.AppsSearchContainerLayout;
import amirz.shade.shadespace.ShadespaceView;
import amirz.shade.transitions.TransitionManager;

import static com.android.launcher3.LauncherState.ALL_APPS;
import static com.android.launcher3.LauncherState.NORMAL;

public class ShadeLauncher extends Launcher {
    private final SearchLauncherCallbacks mCallbacks;
    private enum State {
        STOPPED,
        RECREATE_DEFERRED,
        STARTED
    }
    private State mState = State.STOPPED;

    public ShadeLauncher() {
        mCallbacks = new SearchLauncherCallbacks(this);
        setLauncherCallbacks(mCallbacks);
    }

    public SearchLauncherCallbacks getCallbacks() {
        return mCallbacks;
    }

    private static class SearchLauncherCallbacks
            implements LauncherCallbacks, WallpaperColorInfo.OnChangeListener,
            DeviceProfile.OnDeviceProfileChangeListener,
            SharedPreferences.OnSharedPreferenceChangeListener {
        private final Launcher mLauncher;

        private ShadeOverlay mOverlayCallbacks;
        private LauncherClient mLauncherClient;
        private boolean mDeferCallbacks;
        private final Bundle mPrivateOptions = new Bundle();
        private final Handler mHandler = new Handler();
        private boolean mNoFloatingView;

        private ShadespaceView mShadespace;

        private SearchLauncherCallbacks(Launcher launcher) {
            mLauncher = launcher;
        }

        public void deferCallbacksUntilNextResumeOrStop() {
            mDeferCallbacks = true;
        }

        public LauncherClient getLauncherClient() {
            return mLauncherClient;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            SharedPreferences prefs = Utilities.getPrefs(mLauncher);
            mOverlayCallbacks = new ShadeOverlay(mLauncher);
            mLauncherClient = new LauncherClient(mLauncher, mOverlayCallbacks, getClientOptions(prefs));
            mOverlayCallbacks.setClient(mLauncherClient);

            mLauncher.addOnDeviceProfileChangeListener(this);
            prefs.registerOnSharedPreferenceChangeListener(this);

            WallpaperColorInfo instance = WallpaperColorInfo.getInstance(mLauncher);
            instance.addOnChangeListener(this);
            onExtractedColorsChanged(instance);

            mShadespace = mLauncher.findViewById(R.id.search_container_workspace);
        }

        @Override
        public void onDetachedFromWindow() {
            mLauncherClient.onDetachedFromWindow();
        }

        @Override
        public void onAttachedToWindow() {
            mLauncherClient.onAttachedToWindow();
        }

        @Override
        public void onHomeIntent(boolean internalStateHandled) {
            mLauncherClient.hideOverlay(mLauncher.isStarted() && !mLauncher.isForceInvisible());
            if (mLauncher.hasWindowFocus()
                    && mLauncher.isInState(NORMAL)
                    && mNoFloatingView) {
                ExtendedEditText searchUiManager =
                        (ExtendedEditText) mLauncher.getAppsView().getSearchUiManager();
                mLauncher.getStateManager().goToState(LauncherState.ALL_APPS, true,
                        () -> mHandler.post(searchUiManager::showKeyboard)
                );
            }
        }

        @Override
        public void onDeviceProfileChanged(DeviceProfile deviceProfile) {
            mLauncherClient.reattachOverlay();
        }

        @Override
        public void onResume() {
            Handler handler = mLauncher.getDragLayer().getHandler();
            if (mDeferCallbacks) {
                if (handler == null) {
                    // Finish defer if we are not attached to window.
                    checkIfStillDeferred();
                } else {
                    // Wait one frame before checking as we can get multiple resume-pause events
                    // in the same frame.
                    handler.post(this::checkIfStillDeferred);
                }
            } else {
                mLauncherClient.onResume();
            }
            mShadespace.onResume();
        }

        @Override
        public void onPause() {
            if (!mDeferCallbacks) {
                mLauncherClient.onPause();
            }
            mShadespace.onPause();
            mNoFloatingView = AbstractFloatingView.getTopOpenView(mLauncher) == null;
        }

        @Override
        public void onStart() {
            if (!mDeferCallbacks) {
                mLauncherClient.onStart();
            }
        }

        @Override
        public void onStop() {
            if (mDeferCallbacks) {
                checkIfStillDeferred();
            } else {
                mLauncherClient.onStop();
            }
        }

        private void checkIfStillDeferred() {
            if (!mDeferCallbacks) {
                return;
            }
            if (!mLauncher.hasBeenResumed() && mLauncher.isStarted()) {
                return;
            }
            mDeferCallbacks = false;

            // Move the client to the correct state. Calling the same method twice is no-op.
            if (mLauncher.isStarted()) {
                mLauncherClient.onStart();
            }
            if (mLauncher.hasBeenResumed()) {
                mLauncherClient.onResume();
            } else {
                mLauncherClient.onPause();
            }
            if (!mLauncher.isStarted()) {
                mLauncherClient.onStop();
            }
        }

        @Override
        public void onDestroy() {
            WallpaperColorInfo.getInstance(mLauncher).removeOnChangeListener(this);
            Utilities.getPrefs(mLauncher).unregisterOnSharedPreferenceChangeListener(this);
            mLauncherClient.onDestroy();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) { }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) { }

        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) { }

        @Override
        public void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args) {
            mLauncherClient.dump(prefix, w);
        }

        @Override
        public boolean handleBackPressed() {
            if (!mLauncher.getDragController().isDragging()) {
                AbstractFloatingView topView = AbstractFloatingView.getTopOpenView(mLauncher);
                if (topView != null && topView.onBackPressed()) {
                    // Override base because we do not want to call onBackPressed twice.
                    return true;
                } else if (mLauncher.isInState(ALL_APPS)) {
                    AppsSearchContainerLayout search =
                            (AppsSearchContainerLayout) mLauncher.getAppsView().getSearchUiManager();
                    // Reset the search if it has text in it.
                    if (search.getText().length() > 0) {
                        search.searchString("");
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onTrimMemory(int level) { }

        @Override
        public void onLauncherProviderChange() { }

        @Override
        public void bindAllApplications(ArrayList<AppInfo> apps) { }

        @Override
        public boolean hasSettings() {
            return false;
        }

        @Override
        public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData) {
            return false;
        }

        @Override
        public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
            int alpha = mLauncher.getResources().getInteger(R.integer.extracted_color_gradient_alpha);

            mPrivateOptions.putInt("background_color_hint",
                    primaryColor(wallpaperColorInfo, mLauncher, alpha));
            mPrivateOptions.putInt("background_secondary_color_hint",
                    secondaryColor(wallpaperColorInfo, mLauncher, alpha));
            mPrivateOptions.putBoolean("is_background_dark",
                    Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark));

            mLauncherClient.setPrivateOptions(mPrivateOptions);
        }

        private LauncherClient.ClientOptions getClientOptions(SharedPreferences prefs) {
            return new LauncherClient.ClientOptions(
                    prefs.getBoolean(ShadeSettings.PREF_MINUS_ONE, true),
                    true, /* enableHotword */
                    true /* enablePrewarming */
            );
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if (key.equals(ShadeSettings.PREF_MINUS_ONE)) {
                mLauncherClient.setClientOptions(getClientOptions(prefs));
            }
        }
    }

    private static int primaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(
                wallpaperColorInfo.getMainColor(), alpha), context);
    }

    private static int secondaryColor(WallpaperColorInfo wallpaperColorInfo, Context context, int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(
                wallpaperColorInfo.getSecondaryColor(), alpha), context);
    }

    private static int compositeAllApps(int color, Context context) {
        return ColorUtils.compositeColors(
                Themes.getAttrColor(context, R.attr.allAppsScrimColor), color);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mState == State.RECREATE_DEFERRED) {
            super.recreate();
        }
        mState = State.STARTED;
        TransitionManager manager = (TransitionManager) getAppTransitionManager();
        manager.overrideAppClose(this);
    }

    @Override
    public void recreate() {
        if (mState == State.STARTED) {
            super.recreate();
        } else {
            mState = State.RECREATE_DEFERRED;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mState = State.STOPPED;
    }

    @Override
    protected int getThemeRes(WallpaperColorInfo wallpaperColorInfo) {
        return ShadeSettings.getThemeRes(this, super.getThemeRes(wallpaperColorInfo));
    }
}
