package amirz.shade.icons.calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;

import com.android.launcher3.LauncherModel;
import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import amirz.shade.icons.pack.IconReloader;

/**
 * Listens for date change events and uses the IconReloader to reload all loaded calendar icons
 * when the date has changed.
 */
public class DateChangeReceiver extends BroadcastReceiver {
    private final Set<ComponentKey> mCalendars = new HashSet<>();
    private int mLastDay;

    public DateChangeReceiver(Context context) {
        super();

        IntentFilter filter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        if (!Utilities.ATLEAST_NOUGAT) {
            filter.addAction(Intent.ACTION_TIME_TICK);
        }

        Handler handler = new Handler(LauncherModel.getWorkerLooper());
        context.registerReceiver(this, filter, null, handler);
    }

    public void setCalendar(ComponentKey key, boolean calendar) {
        if (calendar) {
            mCalendars.add(key);
        } else {
            mCalendars.remove(key);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Utilities.ATLEAST_NOUGAT) {
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            if (day == mLastDay) {
                return;
            }
            mLastDay = day;
        }

        IconReloader.get(context).reload(mCalendars.toArray(new ComponentKey[0]));
    }
}
