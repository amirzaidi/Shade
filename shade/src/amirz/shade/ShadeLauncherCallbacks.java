package amirz.shade;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.LauncherCallbacks;
import com.android.launcher3.LauncherState;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.search.AppsSearchContainerLayout;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import amirz.shade.search.AllAppsQsb;

import static amirz.shade.ShadeFont.KEY_OVERRIDE_FONT;
import static com.android.launcher3.LauncherState.ALL_APPS;
import static com.android.launcher3.LauncherState.NORMAL;

public class ShadeLauncherCallbacks implements LauncherCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener,
        DeviceProfile.OnDeviceProfileChangeListener {
    public static final String KEY_ENABLE_MINUS_ONE = "pref_enable_minus_one";

    private final ShadeLauncher mLauncher;

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
        mLauncherClient = new LauncherClient(mLauncher, mOverlayCallbacks, getClientOptions(prefs));
        mOverlayCallbacks.setClient(mLauncherClient);
        prefs.registerOnSharedPreferenceChangeListener(this);
        mLauncher.addOnDeviceProfileChangeListener(this);
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
        } else if (KEY_OVERRIDE_FONT.equals(key)) {
            mLauncher.kill();
        }
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

    @Override
    public void onPause() {
        if (!mDeferCallbacks) {
            mLauncherClient.onPause();
        }
        mNoFloatingView = AbstractFloatingView.getTopOpenView(mLauncher) == null;
    }

    @Override
    public void onDestroy() {
        mLauncherClient.onDestroy();
        Utilities.getPrefs(mLauncher).unregisterOnSharedPreferenceChangeListener(this);
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
                AllAppsQsb search =
                        (AllAppsQsb) mLauncher.getAppsView().getSearchUiManager();
                return search.tryClearSearch();
            }
        }
        return false;
    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void onStateChanged() {

    }

    @Override
    public void onLauncherProviderChange() {

    }

    @Override
    public boolean startSearch(String initialQuery, boolean selectInitialQuery, Bundle appSearchData) {
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
}
