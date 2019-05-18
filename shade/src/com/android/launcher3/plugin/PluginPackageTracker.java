package com.android.launcher3.plugin;

import android.os.UserHandle;

import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;

import java.util.List;

/**
 * Utility class that calls the given runnable when a package changes.
 * Can be used to check for application installs, updates and deletions.
 */
class PluginPackageTracker implements LauncherAppsCompat.OnAppsChangedCallbackCompat {
    private final Runnable mOnUpdate;

    /**
     * Create a new instance with the given runnable.
     * @param onUpdate Runnable called when a package changes.
     */
    PluginPackageTracker(Runnable onUpdate) {
        mOnUpdate = onUpdate;
    }

    @Override
    public void onPackageRemoved(String packageName, UserHandle user) {
        mOnUpdate.run();
    }

    @Override
    public void onPackageAdded(String packageName, UserHandle user) {
        mOnUpdate.run();
    }

    @Override
    public void onPackageChanged(String packageName, UserHandle user) {
        mOnUpdate.run();
    }

    @Override
    public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
        mOnUpdate.run();
    }

    @Override
    public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
        mOnUpdate.run();
    }

    @Override
    public void onPackagesSuspended(String[] packageNames, UserHandle user) {
        mOnUpdate.run();
    }

    @Override
    public void onPackagesUnsuspended(String[] packageNames, UserHandle user) {
        mOnUpdate.run();
    }

    @Override
    public void onShortcutsChanged(String packageName, List<ShortcutInfoCompat> shortcuts,
                                   UserHandle user) {
    }
}
