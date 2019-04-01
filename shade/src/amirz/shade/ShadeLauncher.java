package amirz.shade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.ColorUtils;

import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;
import com.google.android.libraries.gsa.launcherclient.LauncherClientCallbacks;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

import amirz.shade.allapps.search.AppsSearchContainerLayout;
import amirz.shade.shadespace.ShadespaceView;

public class ShadeLauncher extends Launcher {
    private final SearchLauncherCallbacks mCallbacks;

    public ShadeLauncher() {
        mCallbacks = new SearchLauncherCallbacks(this);
        setLauncherCallbacks(mCallbacks);
    }

    public SearchLauncherCallbacks getCallbacks() {
        return mCallbacks;
    }

    private static class OverlayCallbackImpl implements LauncherOverlay, LauncherClientCallbacks {
        private final Launcher mLauncher;

        private LauncherClient mClient;
        private LauncherOverlayCallbacks mLauncherOverlayCallbacks;
        private boolean mWasOverlayAttached = false;

        private OverlayCallbackImpl(Launcher launcher) {
            mLauncher = launcher;
        }

        private void setClient(LauncherClient client) {
            mClient = client;
        }

        @Override
        public void onServiceStateChanged(boolean overlayAttached, boolean hotwordActive) {
            if (overlayAttached != mWasOverlayAttached) {
                mWasOverlayAttached = overlayAttached;
                mLauncher.setLauncherOverlay(overlayAttached ? this : null);
            }
        }

        @Override
        public void onOverlayScrollChanged(float progress) {
            if (mLauncherOverlayCallbacks != null) {
                mLauncherOverlayCallbacks.onScrollChanged(progress);
            }
        }

        @Override
        public void onScrollInteractionBegin() {
            mClient.startMove();
        }

        @Override
        public void onScrollInteractionEnd() {
            mClient.endMove();
        }

        @Override
        public void onScrollChange(float progress, boolean rtl) {
            mClient.updateMove(progress);
        }

        @Override
        public void setOverlayCallbacks(LauncherOverlayCallbacks callbacks) {
            mLauncherOverlayCallbacks = callbacks;
        }
    }

    private static class SearchLauncherCallbacks
            implements LauncherCallbacks, WallpaperColorInfo.OnChangeListener {
        private final Launcher mLauncher;

        private OverlayCallbackImpl mOverlayCallbacks;
        private LauncherClient mLauncherClient;
        private boolean mDeferCallbacks;
        private final Bundle mPrivateOptions = new Bundle();

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
            mOverlayCallbacks = new OverlayCallbackImpl(mLauncher);
            mLauncherClient = new LauncherClient(mLauncher, mOverlayCallbacks, getClientOptions(prefs));
            mOverlayCallbacks.setClient(mLauncherClient);

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
            AppsSearchContainerLayout search =
                    (AppsSearchContainerLayout) mLauncher.getAppsView().getSearchUiManager();
            // Reset the search if it has text in it.
            if (search.getText().length() > 0) {
                search.searchString("");
                return true;
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
                    true,
                    true, /* enableHotword */
                    true /* enablePrewarming */
            );
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
    protected int getThemeRes(WallpaperColorInfo wallpaperColorInfo) {
        switch (Utilities.getPrefs(this).getString(ShadeSettings.PREF_THEME, "")) {
            case "transparent": return R.style.Shade_Transparent;
            case "nature": return R.style.Shade_Nature;
            case "sunset": return R.style.Shade_Sunset;
            case "campfire": return R.style.Shade_Campfire;
            case "twilight": return R.style.Shade_Twilight;
        }

        return super.getThemeRes(wallpaperColorInfo);
    }
}
