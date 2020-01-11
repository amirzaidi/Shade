package amirz.shade.icons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;

import com.android.launcher3.icons.BaseIconFactory;
import com.android.launcher3.util.ComponentKey;

import amirz.shade.icons.pack.IconResolver;

import static com.android.launcher3.icons.BaseIconFactory.CONFIG_HINT_NO_WRAP;

@SuppressWarnings("unused")
public class ThirdPartyIconProvider extends RoundIconProvider {
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
            return fallback.get();
        }
        icon.setChangingConfigurations(icon.getChangingConfigurations() | CONFIG_HINT_NO_WRAP);
        return icon;
    }
}
