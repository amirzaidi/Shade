package amirz.shade;

import android.os.Bundle;

import com.android.launcher3.Launcher;

public class ShadeLauncher extends Launcher {
    private enum State {
        STOPPED,
        RECREATE_DEFERRED,
        KILL_DEFERRED,
        STARTED
    }

    private final ShadeLauncherCallbacks mCallbacks;
    private State mState = State.STOPPED;

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
    public void onStart() {
        super.onStart();
        if (mState == State.KILL_DEFERRED) {
            ShadeRestarter.initiateRestart(this);
        } else if (mState == State.RECREATE_DEFERRED) {
            super.recreate();
        }
        mState = State.STARTED;
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

    public ShadeLauncherCallbacks getCallbacks() {
        return mCallbacks;
    }
}
