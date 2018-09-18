package amirz.shade;

import android.content.Context;
import android.os.UserHandle;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;

import java.util.List;

public class ShadeUtilities {
    public static void reloadPackage(Context context, String pkg, UserHandle user) {
        LauncherModel model = LauncherAppState.getInstance(context).getModel();
        DeepShortcutManager shortcutManager = DeepShortcutManager.getInstance(context);
        reloadPackage(model, shortcutManager, pkg, user);
    }

    private static void reloadPackage(LauncherModel model, DeepShortcutManager shortcutManager,
                                      String pkg, UserHandle user) {
        model.onPackageChanged(pkg, user);
        List<ShortcutInfoCompat> shortcuts = shortcutManager.queryForPinnedShortcuts(pkg, user);
        if (!shortcuts.isEmpty()) {
            model.updatePinnedShortcuts(pkg, shortcuts, user);
        }
    }
}
