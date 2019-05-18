package com.android.launcher3.plugin.button;

import com.android.launcher3.plugin.button.IButtonPluginCallback;

interface IButtonPlugin {
    boolean onHomeIntent(in IButtonPluginCallback cb);
}
