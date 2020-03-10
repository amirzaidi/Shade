package amirz.unread;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public abstract class AutoRegisterReceiver extends BroadcastReceiver {
    private final Runnable mOnReceive;
    private final IntentFilter mFilter;

    public AutoRegisterReceiver(Runnable onReceive) {
        mOnReceive = onReceive;
        mFilter = getFilter();
    }

    public final void onResume(Context context) {
        context.registerReceiver(this, mFilter);
    }

    public final void onPause(Context context) {
        context.unregisterReceiver(this);
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        mOnReceive.run();
    }

    public abstract IntentFilter getFilter();
}
