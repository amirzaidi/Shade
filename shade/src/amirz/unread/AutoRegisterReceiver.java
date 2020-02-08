package amirz.unread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class AutoRegisterReceiver extends BroadcastReceiver {
    protected final Context mContext;
    private final Runnable mOnReceive;
    private final IntentFilter mFilter;

    public AutoRegisterReceiver(Context context, Runnable onReceive) {
        mContext = context;
        mOnReceive = onReceive;
        mFilter = getFilter();
    }

    public final void onResume() {
        mContext.registerReceiver(this, mFilter);
    }

    public final void onPause() {
        mContext.unregisterReceiver(this);
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        mOnReceive.run();
    }

    public abstract IntentFilter getFilter();
}
