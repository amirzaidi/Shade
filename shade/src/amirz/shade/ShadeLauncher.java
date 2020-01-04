package amirz.shade;

import com.android.launcher3.Launcher;

public class ShadeLauncher extends Launcher {
    private final ShadeLauncherCallbacks mCallbacks;

    public ShadeLauncher() {
        super();

        mCallbacks = new ShadeLauncherCallbacks(this);
        setLauncherCallbacks(mCallbacks);
    }
}
