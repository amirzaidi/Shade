package amirz.shade;

import android.content.Context;

import com.android.launcher3.MainProcessInitializer;
import com.android.launcher3.Utilities;
import com.android.launcher3.config.FeatureFlags;

import amirz.shade.customization.IconShapeOverride;

import static com.android.searchlauncher.SmartspaceQsbWidget.KEY_SMARTSPACE;

@SuppressWarnings("unused")
public class ShadeProcessInitializer extends MainProcessInitializer {
    public ShadeProcessInitializer(Context context) {
        FeatureFlags.QSB_ON_FIRST_SCREEN =
                Utilities.getPrefs(context).getBoolean(KEY_SMARTSPACE, true);
        IconShapeOverride.apply(context);
    }
}
