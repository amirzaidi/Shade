package com.android.launcher3.plugin.button;

import android.content.Intent;
import android.os.IBinder;

import com.android.launcher3.plugin.PluginClient;
import com.android.launcher3.plugin.PluginInterface;

import java.net.URISyntaxException;

public class ButtonPluginClient extends PluginClient.Exclusive<IButtonPlugin> {
    private static final PluginInterface INTERFACE = new PluginInterface(
            "com.android.launcher3.plugin.button.IButtonPlugin",
            1
    );

    public boolean onHomeIntent(Callback cb) {
        return getValue(plugin -> plugin.onHomeIntent(new IButtonPluginCallback.Stub() {
            @Override
            public boolean startActivity(String uri) {
                try {
                    return cb.startActivity(Intent.parseUri(uri, Intent.URI_INTENT_SCHEME));
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }), false);
    }

    @Override
    protected PluginInterface getInterface() {
        return INTERFACE;
    }

    @Override
    protected IButtonPlugin stubService(IBinder service) {
        return IButtonPlugin.Stub.asInterface(service);
    }

    public interface Callback {
        boolean startActivity(Intent intent);
    }
}
