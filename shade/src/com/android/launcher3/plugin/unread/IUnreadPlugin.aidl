package com.android.launcher3.plugin.unread;

import com.android.launcher3.plugin.unread.IUnreadPluginCallback;

interface IUnreadPlugin {
    List<String> getText();

    oneway void clickView(in int index, in Bundle launchOptions);

    oneway void addOnChangeListener(in IUnreadPluginCallback cb);

    oneway void removeOnChangeListener(in IUnreadPluginCallback cb);
}
