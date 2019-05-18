package amirz.shade.shortcuts;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Process;

import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.plugin.PluginManager;
import com.android.launcher3.plugin.shortcuts.ShortcutPluginClient;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;

import java.util.ArrayList;
import java.util.List;

class ShortcutExtension {
    private final Context mContext;
    private final LauncherAppsCompat mApps;

    ShortcutExtension(Context context) {
        mContext = context;
        mApps = LauncherAppsCompat.getInstance(context);
    }

    private ShortcutPluginClient getShortcutClient() {
        return PluginManager.getInstance(mContext).getClient(ShortcutPluginClient.class);
    }

    Drawable getShortcutIconDrawable(ShortcutInfoCompat info, int density) {
        ShortcutInfoCompatExt ext = (ShortcutInfoCompatExt) info;
        return ext.getIcon(mContext, density);
    }

    List<ShortcutInfoCompat> getForActivity(String packageName, ComponentName activity) {
        List<ShortcutInfoCompat> out = new ArrayList<>();

        List<LauncherActivityInfo> infoList =
                mApps.getActivityList(packageName, Process.myUserHandle());
        for (LauncherActivityInfo info : infoList) {
            ComponentName cn = info.getComponentName();
            if (activity == null || activity.equals(cn)) {
                for (ShortcutPluginClient.ShortcutWithIcon shortcut
                        : getShortcutClient().queryShortcuts(packageName, activity)) {
                    out.add(new ShortcutInfoCompatExt(cn, shortcut));
                }
            }
        }
        return out;
    }
}
