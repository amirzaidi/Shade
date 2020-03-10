package amirz.unread;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public class BatteryBroadcastReceiver extends AutoRegisterReceiver {
    private final Context mContext;
    private final BatteryManager mManager;

    public BatteryBroadcastReceiver(Context context, Runnable onReceive) {
        super(onReceive);
        mContext = context;
        mManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }

    @Override
    public IntentFilter getFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_POWER_CONNECTED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        return filter;
    }

    public boolean isCharging() {
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = mContext.registerReceiver(null, filter);
        if (batteryStatus == null) {
            return false;
        }
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
    }

    public int getLevel() {
        return mManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }
}
