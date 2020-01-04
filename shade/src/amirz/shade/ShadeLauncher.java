package amirz.shade;

import android.os.Bundle;

import com.android.launcher3.Launcher;

public class ShadeLauncher extends Launcher {
    private final ShadeLauncherCallbacks mCallbacks;

    public ShadeLauncher() {
        super();

        mCallbacks = new ShadeLauncherCallbacks(this);
        setLauncherCallbacks(mCallbacks);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        super.onCreate(savedInstanceState);
    }
}
