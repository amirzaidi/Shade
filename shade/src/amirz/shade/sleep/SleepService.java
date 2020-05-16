package amirz.shade.sleep;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.Utilities;

public class SleepService extends AccessibilityService {
    public static final String SLEEP = BuildConfig.APPLICATION_ID + ".DT2S";
    public static final String SLEEP_PERM = BuildConfig.APPLICATION_ID + ".permission.DT2S";

    private static boolean sRunning;

    public static boolean isRunning() {
        return sRunning;
    }

    private final IntentFilter mSleepFilter = new IntentFilter();
    private final BroadcastReceiver mSleepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Not supported before Pie.
            if (Utilities.ATLEAST_P) {
                performGlobalAction(GLOBAL_ACTION_HOME);
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
            }
        }
    };

    public SleepService() {
        super();
        mSleepFilter.addAction(SLEEP);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mSleepReceiver, mSleepFilter, SLEEP_PERM, new Handler());
        sRunning = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSleepReceiver);
        sRunning = false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
