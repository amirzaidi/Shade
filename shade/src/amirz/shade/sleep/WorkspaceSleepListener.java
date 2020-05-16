package amirz.shade.sleep;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;

import com.android.launcher3.Launcher;
import com.android.launcher3.Workspace;
import com.android.launcher3.touch.WorkspaceTouchListener;

import static amirz.shade.sleep.SleepService.SLEEP;
import static amirz.shade.sleep.SleepService.SLEEP_PERM;

@SuppressLint("ClickableViewAccessibility")
public class WorkspaceSleepListener extends WorkspaceTouchListener {
    private static final String TAG = "WorkspaceSleepListener";

    public static void override(Launcher launcher) {
        Workspace workspace = launcher.getWorkspace();
        workspace.setOnTouchListener(new WorkspaceSleepListener(launcher, workspace));
    }

    private final Launcher mLauncher;
    private final Handler mHandler;

    private WorkspaceSleepListener(Launcher launcher, Workspace workspace) {
        super(launcher, workspace);
        mLauncher = launcher;
        mHandler = new Handler();
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        if (SleepService.isRunning()) {
            Log.d(TAG, "Sending double tap to sleep intent to accessibility service.");
            mLauncher.sendBroadcast(new Intent(SLEEP), SLEEP_PERM);

            MotionEvent ev = MotionEvent.obtain(e);
            mHandler.post(() -> {
                ev.setAction(MotionEvent.ACTION_UP);
                onTouch(mLauncher.getWorkspace(), ev);
                ev.recycle();
            });
            return true;
        }
        return false;
    }
}
