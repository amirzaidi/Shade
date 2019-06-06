package com.android.launcher3.plugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Resources;
import android.os.IBinder;
import android.util.Log;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.plugin.activity.ActivityPluginClient;
import com.android.launcher3.plugin.button.ButtonPluginClient;
import com.android.launcher3.plugin.shortcuts.ShortcutPluginClient;
import com.android.launcher3.plugin.unread.UnreadPluginClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static android.content.Context.BIND_ADJUST_WITH_ACTIVITY;
import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE;

/**
 * Top level plugin handler that keeps track of all connections and persistent state information.
 * Uses a singleton model, with {@code getInstance} as the entry point.
 */
public final class PluginManager {
    private static final String TAG = "PluginManager";

    public static final String PREF_PLUGIN_PREFIX = "pref_plugin";

    private static final String INTENT_ACTION = "com.android.launcher3.PLUGIN";
    private static final String INTERFACE_DESCRIPTOR = "interface.descriptor";
    private static final String INTERFACE_VERSION = "interface.version";

    private static PluginManager sInstance;

    /**
     * Gets the singleton instance, and creates it if it did not exist yet.
     * @param context The context passed to the constructor.
     * @return The singleton instance.
     */
    public static synchronized PluginManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new PluginManager(context.getApplicationContext());
        }
        return sInstance;
    }

    private final Context mContext;
    private final PackageManager mPm;

    private final List<PluginClient> mClients;
    private final Map<ComponentName, Runnable> mUnbinds = new HashMap<>();

    /**
     * Creates a new plugin manager with the given context.
     * @param context The context.
     */
    private PluginManager(Context context) {
        mContext = context;
        mPm = context.getPackageManager();

        List<PluginClient> clients = new ArrayList<>();
        mClients = Collections.unmodifiableList(clients);

        clients.add(new ActivityPluginClient());
        clients.add(new ButtonPluginClient());
        clients.add(new ShortcutPluginClient());
        clients.add(new UnreadPluginClient());

        // Reload connections when a package is installed, changed or deleted.
        LauncherAppsCompat.getInstance(context).addOnAppsChangedCallback(
                new PluginPackageTracker(this::reloadConnections));
    }

    /**
     * Access to the plugin clients for outside classes.
     * Throws IllegalArgumentException if cls is invalid.
     * @param cls Class that is used to resolve.
     * @param <T> Generic type argument.
     * @return The client.
     */
    public <T extends PluginClient> T getClient(Class<T> cls) {
        for (PluginClient client : mClients) {
            if (client.getClass() == cls) {
                //noinspection unchecked
                return (T) client;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Searches through all plugin clients to find one that implements the given plugin.
     * @param plugin The plugin to be used for searching.
     * @return Implementing plugin client, or null if none was found.
     */
    private PluginClient getBaseClient(Plugin plugin) {
        PluginInterface pluginInterface = plugin.getInterface();
        for (PluginClient client : mClients) {
            if (client.getInterface().equals(pluginInterface)) {
                return client;
            }
        }
        return null;
    }

    /**
     * Connects to plugins that are enabled and not connected yet.
     * Disconnects from disabled plugins.
     */
    public void reloadConnections() {
        List<Plugin> plugins = getPlugins();
        Set<ComponentName> toRemove = new HashSet<>(mUnbinds.keySet());
        for (Plugin plugin : plugins) {
            if (plugin.isEnabled() && !toRemove.remove(plugin.getComponent())) {
                // Since this entry did not exist yet, it was not connected.
                // Therefore, we connect now.
                if (!connectToPlugin(plugin)) {
                    Log.d(TAG, "Connect to plugin failed: "
                            + plugin.getComponent().flattenToString());
                }
            }
        }
        for (ComponentName activity : toRemove) {
            mUnbinds.get(activity).run();
        }
    }

    /**
     * Connects to a plugin using the given plugin information.
     * @param plugin The plugin.
     * @return True if a service was bound, false otherwise.
     */
    private boolean connectToPlugin(Plugin plugin) {
        PluginClient client = getBaseClient(plugin);
        if (client != null) {
            ServiceConnection conn = new ServiceConnection() {
                private IBinder mBinder;

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d(TAG, "onServiceConnected " + name.flattenToShortString());
                    mBinder = service;
                    client.onPluginConnected(service);
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d(TAG, "onServiceDisconnected " + name.flattenToShortString());
                    if (mUnbinds.remove(name) != null) {
                        client.onPluginDisconnected(mBinder);
                    }
                }
            };

            boolean success = mContext.bindService(plugin.getIntent(), conn,
                    BIND_AUTO_CREATE | BIND_ADJUST_WITH_ACTIVITY);
            if (success) {
                mUnbinds.put(plugin.getComponent(), () -> {
                    conn.onServiceDisconnected(plugin.getComponent());
                    mContext.unbindService(conn);
                });
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a list of plugins installed on this device.
     * @return List of plugins.
     */
    public List<Plugin> getPlugins() {
        List<ServiceInfo> siList = queryInstalledPlugins();
        List<Plugin> plugins = new ArrayList<>();

        for (ServiceInfo si : siList) {
            if (si.permission == null || hasPermission(si.permission)) {
                try {
                    plugins.add(new Plugin(si));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return plugins;
    }

    private boolean hasPermission(String perm) {
        return mContext.checkCallingOrSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Checks if a plugin implementing the given plugin client class has been enabled.
     * @param cls The type of plugin client.
     * @return True if there is an enabled plugin, false otherwise.
     */
    public boolean hasPluginTypeEnabled(Class<? extends PluginClient> cls) {
        for (Plugin plugin : getPlugins()) {
            PluginClient client = getBaseClient(plugin);
            if (client != null && client.getClass() == cls && plugin.isEnabled()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a list of service information for all installed plugins on this device.
     * @return List of service info.
     */
    private List<ServiceInfo> queryInstalledPlugins() {
        Intent intent = new Intent(INTENT_ACTION);
        List<ServiceInfo> siList = new ArrayList<>();
        for (ResolveInfo ri : mPm.queryIntentServices(intent,
                PackageManager.GET_META_DATA | PackageManager.GET_RESOLVED_FILTER)) {
            ServiceInfo si = ri.serviceInfo;
            if (si != null && si.metaData != null) {
                siList.add(ri.serviceInfo);
            }
        }
        return siList;
    }

    /**
     * Information about a plugin on this device.
     */
    public class Plugin {
        private final PluginInterface mInterface;
        private final String mClientKey;
        private final boolean mExclusive;

        private final ComponentName mComponent;
        private final String mPluginKey;
        private final CharSequence mAppLabel;
        private final boolean mDebuggable;
        private final CharSequence mShortLabel;
        private final CharSequence mLongLabel;

        /**
         * Fill plugin information using given service info.
         * @param si The service info.
         * @throws PackageManager.NameNotFoundException If the service was uninstalled.
         */
        private Plugin(ServiceInfo si) throws PackageManager.NameNotFoundException {
            String descriptor = si.metaData.getString(INTERFACE_DESCRIPTOR);
            int version = si.metaData.getInt(INTERFACE_VERSION);
            mInterface = new PluginInterface(descriptor, version);
            mClientKey = PREF_PLUGIN_PREFIX + "_" + descriptor + "_" + version;

            PluginClient client = getBaseClient(this);
            mExclusive = client != null && client.isExclusive();

            Resources res = mPm.getResourcesForApplication(si.applicationInfo);

            mComponent = new ComponentName(si.packageName, si.name);
            mPluginKey = mComponent.flattenToShortString();
            mAppLabel = si.applicationInfo.loadLabel(mPm);
            mDebuggable = (si.applicationInfo.flags & FLAG_DEBUGGABLE) != 0;
            mShortLabel = si.loadLabel(mPm);
            mLongLabel = res.getString(si.descriptionRes);
        }

        private PluginInterface getInterface() {
            return mInterface;
        }

        private ComponentName getComponent() {
            return mComponent;
        }

        private String getPluginKey() {
            return mPluginKey;
        }

        public boolean isInPackage() {
            return BuildConfig.APPLICATION_ID.equals(mComponent.getPackageName());
        }

        public CharSequence getAppLabel() {
            return mAppLabel;
        }

        public boolean isDebuggable() {
            return mDebuggable;
        }

        public CharSequence getShortLabel() {
            return mShortLabel;
        }

        public CharSequence getLongLabel() {
            return mLongLabel;
        }

        Intent getIntent() {
            return new Intent(INTENT_ACTION).setComponent(mComponent);
        }

        public boolean isEnabled() {
            return getClientPlugins().contains(mPluginKey);
        }

        /**
         * Sets the state of this plugin to be either enabled or disabled.
         * If this plugin is exclusive, setting it to enabled will disable other plugins.
         * @param enable True if the plugin should be enabled, false otherwise.
         */
        public void setEnabled(boolean enable) {
            Set<String> clientPlugins = new HashSet<>(getClientPlugins());
            if (enable) {
                if (mExclusive) {
                    clientPlugins.clear();
                }
                clientPlugins.add(mPluginKey);
            } else {
                clientPlugins.remove(mPluginKey);
            }
            Utilities.getPrefs(mContext).edit().putStringSet(mClientKey, clientPlugins).apply();
        }

        /**
         * Get the list of enabled plugins in the current plugin client.
         * @return The list of plugins.
         */
        private Set<String> getClientPlugins() {
            Set<String> defaultSet = new HashSet<>();
            for (Plugin plugin : getPlugins()) {
                // By default, enable plugins that are in the same package.
                if (plugin.isInPackage() && plugin.getInterface().equals(mInterface)) {
                    defaultSet.add(plugin.getPluginKey());
                }
            }
            return Utilities.getPrefs(mContext).getStringSet(mClientKey, defaultSet);
        }
    }
}
