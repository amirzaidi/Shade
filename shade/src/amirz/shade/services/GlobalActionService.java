package amirz.shade.services;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.accessibility.AccessibilityEvent;

import com.android.launcher3.BuildConfig;

import java.util.Objects;

import static amirz.shade.services.Services.PERM;

@TargetApi(28)
public class GlobalActionService extends AccessibilityService {
    public static final String SLEEP = BuildConfig.APPLICATION_ID + ".DT2S";
    public static final String RECENTS = BuildConfig.APPLICATION_ID + ".RECENTS";

    private static boolean sRunning;

    public static boolean isRunning() {
        return sRunning;
    }

    private final IntentFilter mSleepFilter = new IntentFilter();
    private final BroadcastReceiver mSleepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case SLEEP:
                    performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                    break;
                case RECENTS:
                    performGlobalAction(GLOBAL_ACTION_RECENTS);
                    break;
            }
        }
    };

    public GlobalActionService() {
        super();
        mSleepFilter.addAction(SLEEP);
        mSleepFilter.addAction(RECENTS);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        registerReceiver(mSleepReceiver, mSleepFilter, PERM, new Handler());
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
