package com.android.launcher3.plugin.unread;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.launcher3.plugin.PluginClient;
import com.android.launcher3.plugin.PluginInterface;

import java.util.ArrayList;
import java.util.List;

public class UnreadPluginClient extends PluginClient.Exclusive<IUnreadPlugin> {
    private static final PluginInterface INTERFACE = new PluginInterface(
            "com.android.launcher3.plugin.unread.IUnreadPlugin",
            1
    );

    private final IUnreadPluginCallback.Stub mCallback = new IUnreadPluginCallback.Stub() {
        @Override
        public void onChange() {
            mListener.onChange();
        }
    };

    private UnreadListener mListener;

    public void setListener(UnreadListener listener) {
        if (mListener == null && listener != null) {
            callAll(plugin -> plugin.addOnChangeListener(mCallback));
        } else if (mListener != null && listener == null) {
            callAll(plugin -> plugin.removeOnChangeListener(mCallback));
        }
        mListener = listener;
    }

    public List<String> getText() {
        return getValue(IUnreadPlugin::getText, new ArrayList<>());
    }

    public void clickView(int index, Bundle launchOptions) {
        callAll(plugin -> plugin.clickView(index, launchOptions));
    }

    @Override
    protected PluginInterface getInterface() {
        return INTERFACE;
    }

    @Override
    protected IUnreadPlugin stubService(IBinder service) {
        return IUnreadPlugin.Stub.asInterface(service);
    }

    @Override
    protected void onBound(IUnreadPlugin plugin) throws RemoteException {
        if (mListener != null) {
            plugin.addOnChangeListener(mCallback);
            mListener.onChange();
        }
    }

    public interface UnreadListener {
        void onChange();
    }
}
