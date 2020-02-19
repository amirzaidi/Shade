package amirz.unread.calendar;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.CalendarContract;

import amirz.unread.AutoRegisterReceiver;

public class DateBroadcastReceiver extends AutoRegisterReceiver {
    public DateBroadcastReceiver(Context context, Runnable onReceive) {
        super(context, onReceive);
    }

    @Override
    public IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        return filter;
    }

    public Intent getCalendarIntent() {
        Uri.Builder timeUri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time");
        ContentUris.appendId(timeUri, System.currentTimeMillis());
        return new Intent(Intent.ACTION_VIEW)
                .setData(timeUri.build());
    }
}
