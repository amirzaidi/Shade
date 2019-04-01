package com.google.android.libraries.gsa.launcherclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import amirz.aidlbridge.LauncherClientBridge;

class SimpleServiceConnection implements ServiceConnection {
    private final Context context;
    private final int flags;
    private final ServiceConnection bridge;
    private boolean boundSuccessfully;

    SimpleServiceConnection(Context context, int flags) {
        this.context = context;
        this.flags = flags;
        this.bridge = LauncherClientBridge.wrap(this);
    }

    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
    }

    public void onServiceDisconnected(ComponentName componentName) {
    }

    public void unbindSelf() {
        if (boundSuccessfully) {
            context.unbindService(bridge);
            boundSuccessfully = false;
        }

    }

    public boolean isBound() {
        return boundSuccessfully;
    }

    public final boolean connectSafely() {
        if (!boundSuccessfully) {
            try {
                boundSuccessfully = context.bindService(LauncherClientBridge.getServiceIntent(context), bridge, flags);
            } catch (SecurityException ex) {
                Log.e("LauncherClient", "Unable to connect to overlay service", ex);
            }
        }

        return boundSuccessfully;
    }
}
