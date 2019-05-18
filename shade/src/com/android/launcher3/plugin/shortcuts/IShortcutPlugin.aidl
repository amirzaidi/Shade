package com.android.launcher3.plugin.shortcuts;

interface IShortcutPlugin {
    List<Bundle> queryShortcuts(in String packageName, in ComponentName activity);

    Bitmap getIcon(in String key, in int density);
}
