package amirz.shade.customization;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.os.UserHandle;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.compat.UserManagerCompat;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;
import com.android.launcher3.util.ComponentKey;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppReloader {
    private static AppReloader sInstance;

    public static synchronized AppReloader get(Context context) {
        if (sInstance == null) {
            sInstance = new AppReloader(context);
        }
        return sInstance;
    }

    private final LauncherModel mModel;
    private final UserManagerCompat mUsers;
    private final DeepShortcutManager mShortcuts;
    private final LauncherAppsCompat mApps;

    private AppReloader(Context context) {
        mModel = LauncherAppState.getInstance(context).getModel();
        mUsers = UserManagerCompat.getInstance(context);
        mShortcuts = DeepShortcutManager.getInstance(context);
        mApps = LauncherAppsCompat.getInstance(context);
    }

    public void reload() {
        for (UserHandle user : mUsers.getUserProfiles()) {
            Set<String> pkgsSet = new HashSet<>();
            for (LauncherActivityInfo info : mApps.getActivityList(null, user)) {
                pkgsSet.add(info.getComponentName().getPackageName());
            }
            for (String pkg : pkgsSet) {
                reload(user, pkg);
            }
        }
    }

    public void reload(ComponentKey... keys) {
        for (ComponentKey key : keys) {
            reload(key.user, key.componentName.getPackageName());
        }
    }

    private void reload(UserHandle user, String pkg) {
        mModel.onPackageChanged(pkg, user);
        List<ShortcutInfoCompat> shortcuts = mShortcuts.queryForPinnedShortcuts(pkg, user);
        if (!shortcuts.isEmpty()) {
            mModel.updatePinnedShortcuts(pkg, shortcuts, user);
        }
    }
}
