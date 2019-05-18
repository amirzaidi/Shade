package amirz.plugin.unread;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;

public abstract class DateBroadcastReceiver extends BroadcastReceiver {
    private final Context mContext;
    private final IntentFilter mFilter;

    DateBroadcastReceiver(Context context) {
        super();
        mContext = context;
        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_TIME_TICK);
        mFilter.addAction(Intent.ACTION_TIME_CHANGED);
        mFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
    }

    void onResume() {
        mContext.registerReceiver(this, mFilter);
    }

    void onPause() {
        mContext.unregisterReceiver(this);
    }

    void openCalendar(Bundle activityOptions) {
        Uri.Builder timeUri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time");
        ContentUris.appendId(timeUri, System.currentTimeMillis());
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(timeUri.build())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            mContext.startActivity(intent, activityOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
