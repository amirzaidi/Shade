package amirz.gesture;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.Utilities;

public class GestureService extends AccessibilityService {
    public static final String SLEEP = BuildConfig.APPLICATION_ID + ".DT2S";
    public static final String SLEEP_PERM = BuildConfig.APPLICATION_ID + ".permission.DT2S";

    private final IntentFilter mSleepFilter = new IntentFilter();
    private final BroadcastReceiver mSleepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Not supported before Pie.
            if (Utilities.ATLEAST_P) {
                performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                performGlobalAction(GLOBAL_ACTION_HOME);
            }
        }
    };

    private BarView mView;

    public GestureService() {
        super();
        mSleepFilter.addAction(SLEEP);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mView = new BarView(this);
        registerReceiver(mSleepReceiver, mSleepFilter, SLEEP_PERM, new Handler());
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mSleepReceiver);
    }

    @Override
    protected void onServiceConnected() {
        mView.addToWm();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
