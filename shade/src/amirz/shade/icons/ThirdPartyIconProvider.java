package amirz.shade.icons;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;

import com.android.launcher3.util.ComponentKey;

import amirz.shade.icons.pack.IconResolver;

public class ThirdPartyIconProvider extends RoundIconProvider {
    private final Context mContext;

    public ThirdPartyIconProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Drawable getIcon(LauncherActivityInfo launcherActivityInfo, int iconDpi, boolean flattenDrawable) {
        ComponentKey key = new ComponentKey(
                launcherActivityInfo.getComponentName(), launcherActivityInfo.getUser());

        IconResolver.DefaultDrawableProvider fallback =
                () -> super.getIcon(launcherActivityInfo, iconDpi, flattenDrawable);
        Drawable icon = ThirdPartyIconUtils.getByKey(mContext, key, iconDpi, fallback);
        return icon == null
                ? fallback.get()
                : icon;
    }
}
