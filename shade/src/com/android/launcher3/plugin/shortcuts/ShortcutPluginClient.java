package com.android.launcher3.plugin.shortcuts;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;

import com.android.launcher3.plugin.PluginClient;
import com.android.launcher3.plugin.PluginInterface;

import java.util.ArrayList;
import java.util.List;

public class ShortcutPluginClient extends PluginClient<IShortcutPlugin> {
    private static final PluginInterface INTERFACE = new PluginInterface(
            "com.android.launcher3.plugin.shortcuts.IShortcutPlugin",
            1
    );

    public List<ShortcutWithIcon> queryShortcuts(String packageName, ComponentName activity) {
        return aggregateList(plugin -> wrap(plugin.queryShortcuts(packageName, activity), plugin));
    }

    private List<ShortcutWithIcon> wrap(List<Bundle> bundles, IShortcutPlugin plugin) {
        List<ShortcutWithIcon> shortcutWithIcons = new ArrayList<>();
        for (Bundle bundle : bundles) {
            shortcutWithIcons.add(new ShortcutWithIcon(bundle, plugin));
        }
        return shortcutWithIcons;
    }

    public static class ShortcutWithIcon {
        private final Bundle mBundle;
        private final IShortcutPlugin mPlugin;

        private ShortcutWithIcon(Bundle bundle, IShortcutPlugin plugin) {
            mBundle = bundle;
            mPlugin = plugin;
        }

        public Bundle getBundle() {
            return mBundle;
        }

        public Bitmap getIcon(int density) {
            try {
                return mPlugin.getIcon(mBundle.getString("key"), density);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    protected PluginInterface getInterface() {
        return INTERFACE;
    }

    @Override
    protected IShortcutPlugin stubService(IBinder service) {
        return IShortcutPlugin.Stub.asInterface(service);
    }
}
