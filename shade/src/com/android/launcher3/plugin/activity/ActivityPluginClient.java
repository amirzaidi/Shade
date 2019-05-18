package com.android.launcher3.plugin.activity;

import android.os.IBinder;
import android.os.RemoteException;

import com.android.launcher3.plugin.PluginClient;
import com.android.launcher3.plugin.PluginInterface;

public class ActivityPluginClient extends PluginClient<IActivityPlugin> {
    private static final PluginInterface INTERFACE = new PluginInterface(
            "com.android.launcher3.plugin.activity.IActivityPlugin",
            1
    );

    public static final int STATE_CREATED = 1;
    public static final int STATE_STARTED = 2;
    public static final int STATE_RESUMED = 4;
    public static final int STATE_ATTACHED = 8;

    private int mState;

    public void addStateFlag(int state) {
        mState |= state;
        updateState();
    }

    public void removeStateFlag(int state) {
        mState &= ~state;
        updateState();
    }

    private void updateState() {
        callAll(plugin -> plugin.setState(mState));
    }

    @Override
    protected PluginInterface getInterface() {
        return INTERFACE;
    }

    @Override
    protected IActivityPlugin stubService(IBinder service) {
        return IActivityPlugin.Stub.asInterface(service);
    }

    @Override
    protected void onBound(IActivityPlugin plugin) throws RemoteException {
        plugin.clearState();
        plugin.setState(mState);
    }
}
