package amirz.shade.views;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;

import java.util.Calendar;

public class AutoUpdateTextClock {
    private static final String FORMAT = "EEEE, MMM d";

    private Calendar mTime;
    private boolean mRegistered;

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                loadCalendarTimeZone();
            }
            onTimeChanged();
        }
    };

    private final DoubleShadowTextView mTarget;

    public AutoUpdateTextClock(DoubleShadowTextView target) {
        mTarget = target;
    }

    private void loadCalendarTimeZone() {
        mTime = Calendar.getInstance();
    }

    private void onTimeChanged() {
        mTime.setTimeInMillis(System.currentTimeMillis());
        mTarget.updateText(DateFormat.format(FORMAT, mTime));
    }

    public void registerReceiver() {
        if (!mRegistered) {
            mRegistered = true;
            final IntentFilter filter = new IntentFilter();

            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            mTarget.getContext().registerReceiver(mIntentReceiver, filter);
            loadCalendarTimeZone();
            onTimeChanged();
        }
    }

    public void unregisterReceiver() {
        if (mRegistered) {
            mRegistered = false;
            mTarget.getContext().unregisterReceiver(mIntentReceiver);
        }
    }
}
