package amirz.plugin.unread;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

class BatteryBroadcastReceiver extends AutoRegisterReceiver {
    private final BatteryManager mManager;

    BatteryBroadcastReceiver(Context context, Runnable onReceive) {
        super(context, onReceive);
        mManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }

    @Override
    IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        return filter;
    }

    boolean isCharging() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, filter);
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    int getLevel() {
        return mManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }
}
