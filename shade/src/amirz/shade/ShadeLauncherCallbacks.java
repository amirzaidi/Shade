package amirz.shade;

import android.app.SearchManager;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.UserHandle;
import android.text.TextUtils;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.AppInfo;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import amirz.aidlbridge.LauncherClientIntent;
import amirz.shade.animations.TransitionManager;
import amirz.shade.hidden.HiddenAppsDrawerState;
import amirz.shade.icons.pack.IconPackManager;
import amirz.shade.search.AllAppsQsb;
import amirz.shade.sleep.WorkspaceSleepListener;
import amirz.unread.UnreadSession;

import static amirz.shade.ShadeFont.DEFAULT_FONT;
import static amirz.shade.ShadeFont.KEY_FONT;
import static amirz.shade.customization.DockSearch.KEY_DOCK_SEARCH;
import static amirz.shade.customization.ShadeStyle.KEY_THEME;
import static amirz.shade.animations.TransitionManager.KEY_FADING_TRANSITION;
import static com.android.launcher3.LauncherState.ALL_APPS;
import static com.android.launcher3.LauncherState.NORMAL;
import static com.android.launcher3.allapps.PersonalWorkSlidingTabStrip.KEY_SHOWED_PEEK_WORK_TAB;
import static com.android.launcher3.settings.SettingsActivity.GRID_OPTIONS_PREFERENCE_KEY;
import static com.android.launcher3.util.Themes.KEY_DEVICE_THEME;
import static com.android.searchlauncher.SmartspaceQsbWidget.KEY_SMARTSPACE;

public class ShadeLauncherCallbacks implements LauncherCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener,
        DeviceProfile.OnDeviceProfileChangeListener,
        WallpaperColorInfo.OnChangeListener {
    public static final String KEY_ENABLE_MINUS_ONE = "pref_enable_minus_one";
    public static final String KEY_FEED_PROVIDER = "pref_feed_provider";
    private static final String KEY_IDP_GRID_NAME = "idp_grid_name";

    private final ShadeLauncher mLauncher;
    private final Handler mHandler = new Handler();
    private final Bundle mPrivateOptions = new Bundle();

    private LauncherClient mLauncherClient;
    private ShadeLauncherOverlay mOverlayCallbacks;
    private boolean mDeferCallbacks;

    private boolean mNoFloatingView;

    ShadeLauncherCallbacks(ShadeLauncher launcher) {
        mLauncher = launcher;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = Utilities.getPrefs(mLauncher);
        mOverlayCallbacks = new ShadeLauncherOverlay(mLauncher);
        LauncherClientIntent.setPackage(getRecommendedFeedPackage());
        mLauncherClient = new LauncherClient(mLauncher, mOverlayCallbacks, getClientOptions(prefs));
        mOverlayCallbacks.setClient(mLauncherClient);
        WallpaperColorInfo instance = WallpaperColorInfo.getInstance(mLauncher);
        instance.addOnChangeListener(this);
        onExtractedColorsChanged(instance);
        setDefaultValues(prefs);
        prefs.registerOnSharedPreferenceChangeListener(this);
        mLauncher.addOnDeviceProfileChangeListener(this);
        UnreadSession.getInstance(mLauncher).onCreate();
        WorkspaceSleepListener.override(mLauncher);
    }

    private String getRecommendedFeedPackage() {
        String recommended = LauncherClientIntent.getRecommendedPackage(mLauncher);
        String override = Utilities.getPrefs(mLauncher).getString(KEY_FEED_PROVIDER, recommended);
        return TextUtils.isEmpty(override)
                ? recommended
                : override;
    }

    public void deferCallbacksUntilNextResumeOrStop() {
        mDeferCallbacks = true;
    }

    public LauncherClient getLauncherClient() {
        return mLauncherClient;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (KEY_ENABLE_MINUS_ONE.equals(key)) {
            mLauncherClient.setClientOptions(getClientOptions(prefs));
        } else if (KEY_FEED_PROVIDER.equals(key)) {
            // If the launcher should reconnect to a different package, restart it.
            if (!LauncherClientIntent.getPackage().equals(getRecommendedFeedPackage())) {
                mLauncher.kill();
            }
        } else if (KEY_FADING_TRANSITION.equals(key)) {
            TransitionManager transitions = (TransitionManager) mLauncher.getAppTransitionManager();
            transitions.applyWindowPreference(mLauncher);
        } else if (KEY_DEVICE_THEME.equals(key) || KEY_THEME.equals(key) || KEY_FONT.equals(key)) {
            mLauncher.recreate();
        } else if (KEY_SMARTSPACE.equals(key) || KEY_IDP_GRID_NAME.equals(key)
                || KEY_DOCK_SEARCH.equals(key)) {
            mLauncher.kill();
        }
    }

    private void setDefaultValues(SharedPreferences prefs) {
        prefs.edit().putBoolean(KEY_SMARTSPACE, prefs.getBoolean(KEY_SMARTSPACE, true))
                .putString(KEY_FONT, prefs.getString(KEY_FONT, DEFAULT_FONT))
                .putString(KEY_DOCK_SEARCH, prefs.getString(KEY_DOCK_SEARCH, ""))
                .putString(KEY_IDP_GRID_NAME, prefs.getString(KEY_IDP_GRID_NAME, null))
                .putString(KEY_FEED_PROVIDER, prefs.getString(KEY_FEED_PROVIDER,
                        getRecommendedFeedPackage()))
                .putBoolean(GRID_OPTIONS_PREFERENCE_KEY, true)
                .apply();

        // Removes the permanent bounce when there is a work profile but no work apps.
        if (UserManagerCompat.getInstance(mLauncher).hasWorkProfile() && !hasWorkApp()) {
            prefs.edit().putBoolean(KEY_SHOWED_PEEK_WORK_TAB, true).apply();
        }
    }

    private boolean hasWorkApp() {
        AllAppsStore store = mLauncher.getAppsView().getAppsStore();
        UserHandle myUser = Process.myUserHandle();
        for (AppInfo info : store.getApps()) {
            if (!info.user.equals(myUser)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDeviceProfileChanged(DeviceProfile dp) {
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

        UnreadSession.getInstance(mLauncher).onResume(mLauncher);
        if (!mLauncher.getAppsView().getApps().hasFilter()) {
            HiddenAppsDrawerState.getInstance(mLauncher).setRevealed(false);
        }
    }

    @Override
    public void onStart() {
        if (!mDeferCallbacks) {
            mLauncherClient.onStart();
        }

        TransitionManager transitions = (TransitionManager) mLauncher.getAppTransitionManager();
        transitions.applyWindowPreference(mLauncher);
    }

    @Override
    public void onStop() {
        if (mDeferCallbacks) {
            checkIfStillDeferred();
        } else {
            mLauncherClient.onStop();
        }
    }

    @Override
    public void onPause() {
        if (!mDeferCallbacks) {
            mLauncherClient.onPause();
        }
        UnreadSession.getInstance(mLauncher).onPause(mLauncher);
        mNoFloatingView = AbstractFloatingView.getTopOpenView(mLauncher) == null;
    }

    @Override
    public void onDestroy() {
        WallpaperColorInfo.getInstance(mLauncher).removeOnChangeListener(this);
        mLauncherClient.onDestroy();
        Utilities.getPrefs(mLauncher).unregisterOnSharedPreferenceChangeListener(this);
        UnreadSession.getInstance(mLauncher).onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

    }

    @Override
    public void onAttachedToWindow() {
        mLauncherClient.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        mLauncherClient.onDetachedFromWindow();
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter w, String[] args) {
        mLauncherClient.dump(prefix, w);
    }

    @Override
    public void onHomeIntent(boolean internalStateHandled) {
        mLauncherClient.hideOverlay(mLauncher.isStarted() && !mLauncher.isForceInvisible());

        if (mLauncher.hasWindowFocus()
                && mLauncher.isInState(NORMAL)
                && mLauncher.getWorkspace().getNextPage() == 0
                && mNoFloatingView) {
            AllAppsQsb search =
                    (AllAppsQsb) mLauncher.getAppsView().getSearchView();
            search.requestSearch();
            mLauncher.getStateManager().goToState(ALL_APPS, true);
        }
    }

    @Override
    public boolean handleBackPressed() {
        if (!mLauncher.getDragController().isDragging()) {
            AbstractFloatingView topView = AbstractFloatingView.getTopOpenView(mLauncher);
            if (topView != null && topView.onBackPressed()) {
                // Override base because we do not want to call onBackPressed twice.
                return true;
            } else if (mLauncher.isInState(ALL_APPS)) {
                AllAppsQsb search = (AllAppsQsb) mLauncher.getAppsView().getSearchUiManager();
                return search.tryClearSearch();
            }
        }
        return false;
    }

    @Override
    public void onTrimMemory(int level) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            IconPackManager.get(mLauncher).trimMemory();
        }
    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public void onLauncherProviderChange() {

    }

    @Override
    public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData) {
        SearchManager sm = mLauncher.getSystemService(SearchManager.class);
        if (sm == null || sm.getGlobalSearchActivity() == null) {
            AllAppsQsb search = (AllAppsQsb) mLauncher.getAppsView().getSearchView();
            search.requestSearch();
            mHandler.post(() -> mLauncher.getStateManager().goToState(ALL_APPS, true));
            return true;
        }
        return false;
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

    private LauncherClient.ClientOptions getClientOptions(SharedPreferences prefs) {
        return new LauncherClient.ClientOptions(
                prefs.getBoolean(KEY_ENABLE_MINUS_ONE, true),
                true, /* enableHotword */
                true /* enablePrewarming */
        );
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        if (updatePrivateOptions(wallpaperColorInfo)) {
            mLauncher.getWorkspace().runOnOverlayHidden(
                    () -> mLauncherClient.setPrivateOptions(mPrivateOptions));
        }
    }

    private boolean updatePrivateOptions(WallpaperColorInfo wallpaperColorInfo) {
        boolean colorsChanged = false;
        int alpha = mLauncher.getResources().getInteger(R.integer.extracted_color_gradient_alpha);

        int primaryColor = primaryColor(wallpaperColorInfo, mLauncher, alpha);
        if (mPrivateOptions.getInt("background_color_hint", 0) != primaryColor) {
            mPrivateOptions.putInt("background_color_hint", primaryColor);
            colorsChanged = true;
        }

        int secondaryColor = secondaryColor(wallpaperColorInfo, mLauncher, alpha);
        if (mPrivateOptions.getInt("background_secondary_color_hint", 0) != secondaryColor) {
            mPrivateOptions.putInt("background_secondary_color_hint", secondaryColor);
            colorsChanged = true;
        }

        boolean isDark = Themes.getAttrBoolean(mLauncher, R.attr.isMainColorDark);
        if (!mPrivateOptions.containsKey("is_background_dark")
                || mPrivateOptions.getBoolean("is_background_dark") != isDark) {
            mPrivateOptions.putBoolean("is_background_dark", isDark);
            colorsChanged = true;
        }

        return colorsChanged;
    }

    private static int primaryColor(WallpaperColorInfo wallpaperColorInfo, Context context,
                                    int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(
                wallpaperColorInfo.getMainColor(), alpha), context);
    }

    private static int secondaryColor(WallpaperColorInfo wallpaperColorInfo, Context context,
                                      int alpha) {
        return compositeAllApps(ColorUtils.setAlphaComponent(
                wallpaperColorInfo.getSecondaryColor(), alpha), context);
    }

    private static int compositeAllApps(int color, Context context) {
        return ColorUtils.compositeColors(
                Themes.getAttrColor(context, R.attr.allAppsScrimColor), color);
    }
}
