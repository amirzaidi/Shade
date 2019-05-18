package com.android.launcher3.plugin;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Client that handles one type of plugin.
 * @param <T> The plugin type as an AIDL Stub class.
 */
public abstract class PluginClient<T extends IInterface> {
    final Map<IBinder, T> mPlugins = new HashMap<>();

    /**
     * Handles a new binder that implements the plugin type supported by this client.
     * @param service The connected binder.
     */
    final void onPluginConnected(IBinder service) {
        T plugin = stubService(service);
        mPlugins.put(service, plugin);

        try {
            onBound(plugin);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles a disconnected binder that was previously given to {@code onPluginConnected}.
     * @param service The disconnected binder.
     */
    final void onPluginDisconnected(IBinder service) {
        try {
            onUnbound(mPlugins.remove(service));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the interface for the plugin type that this client supports.
     * Should be implemented by extending classes using a static constant field.
     * @return The plugin interface, with descriptor name and version number.
     */
    protected abstract PluginInterface getInterface();

    /**
     * Converts a binder into an AIDL Stub type that the extending class can easily work with.
     * @param service The binder that has to be converted.
     * @return Stub class handling IPC.
     */
    protected abstract T stubService(IBinder service);

    /**
     * Checks if only one plugin can be enabled in this client.
     * @return True if there can only be one, false if there can be more than one.
     */
    boolean isExclusive() {
        return false;
    }

    /**
     * Called when a new plugin has been bound.
     * Extend to setup newly connected plugin.
     * @param plugin The new plugin.
     * @throws RemoteException When plugin throws exceptions.
     */
    protected void onBound(T plugin) throws RemoteException {
    }

    /**
     * Called when a previously connected plugin has been unbound.
     * Extend to handle this case.
     * @param plugin The disconnected plugin.
     * @throws RemoteException When another plugin throws exceptions.
     */
    protected void onUnbound(T plugin) throws RemoteException {
    }

    /**
     * Call all connected plugins with a lambda that does not return a value.
     * @param call The lambda that will be called.
     */
    protected final void callAll(VoidCall<T> call) {
        for (T plugin : mPlugins.values()) {
            try {
                call.run(plugin);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Request a list of a type from all connected plugins,
     * then aggregate the lists into one collection.
     * @param call The lambda that will be used to request the list from each plugin.
     * @param <R> The type of elements in each individual list.
     * @return An aggregated list of all lists, containing elements of type {@code <R>}.
     */
    protected final <R> List<R> aggregateList(ListCall<T, R> call) {
        List<R> result = new ArrayList<>();
        for (T plugin : mPlugins.values()) {
            try {
                result.addAll(call.getList(plugin));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Lambda that has as argument the plugin of type {@code <T>}.
     * @param <T> The plugin type, inferred in calls to the plugin client.
     */
    protected interface VoidCall<T> {
        void run(T plugin) throws RemoteException;
    }

    /**
     * Lambda that has as argument the plugin of type {@code <T>}.
     * @param <T> The plugin type, inferred in calls to the plugin client.
     * @param <R> Tbe resulting element type in the list.
     */
    protected interface ListCall<T, R> {
        List<R> getList(T plugin) throws RemoteException;
    }

    /**
     * Plugin client that only handles one plugin connection at a time.
     * Supports an additional call that returns a non-list value from the connected plugin.
     */
    public static abstract class Exclusive<T extends IInterface> extends PluginClient<T> {
        /**
         * Get a value from the connected plugin, or use a default value if none is connected.
         * @param call The lambda that will be used to request the value from the connected plugin.
         * @param defaultValue The fallback value if no plugin is connected.
         * @param <R> The type of the return value.
         * @return Lambda return value if a plugin is connected, else the default value.
         */
        protected <R> R getValue(ExclusiveCall<T, R> call, R defaultValue) {
            Iterator<T> iterator = mPlugins.values().iterator();
            if (iterator.hasNext()) {
                try {
                    return call.getValue(iterator.next());
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return defaultValue;
        }

        /**
         * Lambda that has as argument the plugin of type {@code <T>}.
         * @param <T> The plugin type, inferred in calls to the plugin client.
         * @param <R> The resulting type of the lambda.
         */
        protected interface ExclusiveCall<T, R> {
            R getValue(T plugin) throws RemoteException;
        }

        @Override
        boolean isExclusive() {
            return true;
        }
    }
}
