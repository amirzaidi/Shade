package amirz.shade;

import android.os.Bundle;

import com.android.launcher3.Launcher;

public class ShadeLauncher extends Launcher {
    private enum State {
        PAUSED,
        RECREATE_DEFERRED,
        KILL_DEFERRED,
        RESUMED
    }

    private final ShadeLauncherCallbacks mCallbacks;
    private State mState = State.PAUSED;

    public ShadeLauncher() {
        super();

        mCallbacks = new ShadeLauncherCallbacks(this);
        setLauncherCallbacks(mCallbacks);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ShadeRestarter.cancelRestart(this);
        ShadeFont.override(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mState == State.KILL_DEFERRED) {
            ShadeRestarter.initiateRestart(this);
        } else if (mState == State.RECREATE_DEFERRED) {
            super.recreate();
        }
        mState = State.RESUMED;
    }

    @Override
    public void recreate() {
        if (mState == State.RESUMED) {
            super.recreate();
        } else if (mState != State.KILL_DEFERRED) {
            mState = State.RECREATE_DEFERRED;
        }
    }

    public void kill() {
        if (mState == State.RESUMED) {
            ShadeRestarter.initiateRestart(this);
        } else {
            mState = State.KILL_DEFERRED;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mState = State.PAUSED;
    }

    public ShadeLauncherCallbacks getCallbacks() {
        return mCallbacks;
    }
}
