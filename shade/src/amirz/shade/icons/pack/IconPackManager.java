package amirz.shade.icons.pack;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;

import com.android.launcher3.util.ComponentKey;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import amirz.shade.customization.CustomizationDatabase;

public class IconPackManager {
    private static final String[] ICON_INTENT_ACTIONS = new String[] {
            "com.fede.launcher.THEME_ICONPACK",
            "com.anddoes.launcher.THEME",
            "com.novalauncher.THEME",
            "com.teslacoilsw.launcher.THEME",
            "com.gau.go.launcherex.theme",
            "org.adw.launcher.THEMES",
            "org.adw.launcher.icons.ACTION_PICK_ICON"
    };

    private static final Intent[] ICON_INTENTS = new Intent[ICON_INTENT_ACTIONS.length];
    private static IconPackManager sInstance;

    static {
        for (int i = 0; i < ICON_INTENT_ACTIONS.length; i++) {
            ICON_INTENTS[i] = new Intent(ICON_INTENT_ACTIONS[i]);
        }
    }

    public static synchronized IconPackManager get(Context context) {
        if (sInstance == null) {
            sInstance = new IconPackManager(context);
        }
        return sInstance;
    }

    private final Context mContext;
    private final Map<String, IconPack> mProviders = new HashMap<>();

    private IconPackManager(Context context) {
        mContext = context;
        reloadProviders();
    }

    private void reloadProviders() {
        PackageManager pm = mContext.getPackageManager();
        Set<ResolveInfo> info = new HashSet<>();
        for (Intent intent : ICON_INTENTS) {
            info.addAll(pm.queryIntentActivities(intent, PackageManager.GET_META_DATA));
        }

        // Remove unavailable packs
        for (String packageName : mProviders.keySet()) {
            boolean foundPackageName = false;
            for (ResolveInfo ri : info) {
                if (ri.activityInfo.packageName.equals(packageName)) {
                    foundPackageName = true;
                    break;
                }
            }
            if (!foundPackageName) {
                mProviders.remove(packageName);
            }
        }

        // Add new packs
        for (ResolveInfo ri : info) {
            String packageName = ri.activityInfo.packageName;
            if (!mProviders.containsKey(packageName)) {
                ApplicationInfo ai = ri.activityInfo.applicationInfo;
                CharSequence label = ri.loadLabel(pm);
                mProviders.put(packageName, new IconPack(ai, label));
            }
        }
    }

    public Map<String, CharSequence> getProviderNames() {
        Map<String, CharSequence> providerTitles = new HashMap<>();
        for (Map.Entry<String, IconPack> pack : mProviders.entrySet()) {
            providerTitles.put(pack.getKey(), pack.getValue().getTitle());
        }
        return providerTitles;
    }

    public IconResolver resolve(ComponentName component, UserHandle user) {
        String packPackage = CustomizationDatabase.getIconPack(
                mContext, new ComponentKey(component, user));

        if (mProviders.containsKey(packPackage)) {
            try {
                IconPack pack = mProviders.get(packPackage);
                IconPack.Data data = pack.getData(mContext.getPackageManager());
                if (data.drawables.containsKey(component)) {
                    int iconId = data.drawables.get(component);
                    return new IconResolver(mContext.getPackageManager(), pack.getAi(), iconId);
                }
            } catch (PackageManager.NameNotFoundException | XmlPullParserException | IOException ignored) {
            }
        }

        return new IconResolver();
    }
}
