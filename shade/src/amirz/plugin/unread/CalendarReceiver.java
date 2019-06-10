package amirz.plugin.unread;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.CalendarContract;

class CalendarReceiver extends AutoRegisterReceiver {
    CalendarReceiver(Context context, Runnable onReceive) {
        super(context, onReceive);
    }

    @Override
    IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PROVIDER_CHANGED);
        filter.addAction(CalendarContract.ACTION_EVENT_REMINDER);
        filter.addDataScheme("content");
        filter.addDataAuthority(CalendarContract.AUTHORITY, null);
        return filter;
    }
}
