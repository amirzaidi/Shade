package amirz.shade;

import com.android.launcher3.Launcher;
import com.google.android.libraries.gsa.launcherclient.LauncherClient;
import com.google.android.libraries.gsa.launcherclient.LauncherClientCallbacks;

public class ShadeLauncherOverlay implements Launcher.LauncherOverlay, LauncherClientCallbacks {
    private final Launcher mLauncher;

    private LauncherClient mClient;
    private Launcher.LauncherOverlayCallbacks mLauncherOverlayCallbacks;
    private boolean mWasOverlayAttached = false;

    public ShadeLauncherOverlay(Launcher launcher) {
        mLauncher = launcher;
    }

    public void setClient(LauncherClient launcherClient) {
        mClient = launcherClient;
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
    public void setOverlayCallbacks(Launcher.LauncherOverlayCallbacks launcherOverlayCallbacks) {
        mLauncherOverlayCallbacks = launcherOverlayCallbacks;
    }
}
