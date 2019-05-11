package com.google.android.libraries.gsa.launcherclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import amirz.aidlbridge.LauncherClientBridge;
import amirz.shade.feed.FeedProviders;

class SimpleServiceConnection implements ServiceConnection {
    private final Context context;
    private final int flags;
    private final ServiceConnection bridge;
    private boolean boundSuccessfully;

    SimpleServiceConnection(Context context, int flags) {
        this.context = context;
        this.flags = flags;
        this.bridge = new LauncherClientBridge(this);
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
                Intent service = FeedProviders.getServiceIntent(context);
                boundSuccessfully = !TextUtils.isEmpty(service.getPackage())
                        && context.bindService(service, bridge, flags);
            } catch (SecurityException ex) {
                Log.e("LauncherClient", "Unable to connect to overlay service", ex);
            }
        }

        return boundSuccessfully;
    }
}
