package amirz.shade.views;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.util.Calendar;

import static android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_WEEKDAY;

public class AutoUpdateTextClock {
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
    private final CharSequence mFormat;

    public AutoUpdateTextClock(DoubleShadowTextView target, CharSequence format) {
        mTarget = target;
        mFormat = format;
    }

    private void loadCalendarTimeZone() {
        mTime = Calendar.getInstance();
    }

    private void onTimeChanged() {
        if (mFormat == null) {
            mTarget.updateText(DateUtils.formatDateTime(
                    mTarget.getContext(), System.currentTimeMillis(),
                    FORMAT_SHOW_WEEKDAY | FORMAT_SHOW_DATE | FORMAT_ABBREV_MONTH));
        } else {
            mTime.setTimeInMillis(System.currentTimeMillis());
            mTarget.updateText(DateFormat.format(mFormat, mTime));
        }
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
