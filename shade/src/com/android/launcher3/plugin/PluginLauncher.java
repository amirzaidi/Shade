package com.android.launcher3.plugin;

import android.os.Bundle;

import com.android.launcher3.Launcher;
import com.android.launcher3.plugin.activity.ActivityPluginClient;

import static com.android.launcher3.plugin.activity.ActivityPluginClient.*;

/**
 * Launcher activity extension that sends state information to {@link ActivityPluginClient}.
 */
public abstract class PluginLauncher extends Launcher {
    private ActivityPluginClient mActivityClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PluginManager manager = PluginManager.getInstance(this);
        manager.reloadConnections();

        mActivityClient = manager.getClient(ActivityPluginClient.class);
        mActivityClient.addStateFlag(STATE_CREATED);
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivityClient.addStateFlag(STATE_STARTED);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivityClient.addStateFlag(STATE_RESUMED);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mActivityClient.addStateFlag(STATE_ATTACHED);
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mActivityClient.removeStateFlag(STATE_ATTACHED);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivityClient.removeStateFlag(STATE_RESUMED);
    }

    @Override
    public void onStop() {
        super.onStop();
        mActivityClient.removeStateFlag(STATE_STARTED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mActivityClient.removeStateFlag(STATE_CREATED);
    }
}
