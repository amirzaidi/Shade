package amirz.plugin.unread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

public abstract class BatteryBroadcastReceiver extends BroadcastReceiver {
    private final Context mContext;
    private final BatteryManager mManager;
    private final IntentFilter mFilter;

    BatteryBroadcastReceiver(Context context) {
        super();
        mContext = context;
        mManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        mFilter = new IntentFilter();
        mFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
    }

    void onResume() {
        mContext.registerReceiver(this, mFilter);
    }

    void onPause() {
        mContext.unregisterReceiver(this);
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
