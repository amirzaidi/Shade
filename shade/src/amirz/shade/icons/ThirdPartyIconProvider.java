package amirz.shade.icons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;

import com.android.launcher3.Utilities;
import com.android.launcher3.icons.AdaptiveIconCompat;
import com.android.launcher3.util.ComponentKey;

import amirz.shade.hidden.HiddenAppsDatabase;
import amirz.shade.icons.pack.IconResolver;

import static com.android.launcher3.icons.BaseIconFactory.CONFIG_HINT_NO_WRAP;
import static com.android.launcher3.icons.BaseIconFactory.CONFIG_HINT_NO_DRAG;

@SuppressWarnings("unused")
public class ThirdPartyIconProvider extends RoundIconProvider {
    private static final int HIDDEN_ALPHA = 0x30;

    private final Context mContext;

    public ThirdPartyIconProvider(Context context) {
        super(context);
        mContext = context;
    }

    @SuppressLint("WrongConstant")
    @Override
    public Drawable getIcon(LauncherActivityInfo launcherActivityInfo, int iconDpi, boolean flattenDrawable) {
        ComponentKey key = new ComponentKey(
                launcherActivityInfo.getComponentName(), launcherActivityInfo.getUser());

        IconResolver.DefaultDrawableProvider fallback =
                () -> super.getIcon(launcherActivityInfo, iconDpi, flattenDrawable);
        Drawable icon = ThirdPartyIconUtils.getByKey(mContext, key, iconDpi, fallback);
        if (icon == null) {
            icon = fallback.get();
        } else {
            icon.setChangingConfigurations(
                    icon.getChangingConfigurations() | CONFIG_HINT_NO_WRAP);
        }
        if (HiddenAppsDatabase.isHidden(mContext, key.componentName, key.user)) {
            icon.setAlpha(HIDDEN_ALPHA);
            icon.setChangingConfigurations(
                    icon.getChangingConfigurations() | CONFIG_HINT_NO_DRAG);
        }
        return Utilities.ATLEAST_OREO && icon instanceof AdaptiveIconDrawable
                    ? AdaptiveIconCompat.wrap((AdaptiveIconDrawable) icon)
                    : icon;
    }
}
