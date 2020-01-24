package amirz.shade.icons;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;

import com.android.launcher3.util.ComponentKey;

import amirz.shade.hidden.HiddenAppsDatabase;
import amirz.shade.icons.pack.IconResolver;

import static com.android.launcher3.icons.BaseIconFactory.CONFIG_HINT_NO_WRAP;

@SuppressWarnings("unused")
public class ThirdPartyIconProvider extends RoundIconProvider {
    // Hidden Brightness
    private static final float HB = 0.75f / 3f;
    private static final ColorFilter HIDDEN_FILTER = new ColorMatrixColorFilter(new float[] {
            HB, HB, HB, 0f, 0f,
            HB, HB, HB, 0f, 0f,
            HB, HB, HB, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
    });

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
            icon.setChangingConfigurations(icon.getChangingConfigurations() | CONFIG_HINT_NO_WRAP);
        }
        if (HiddenAppsDatabase.isHidden(mContext, key.componentName, key.user)) {
            icon.setColorFilter(HIDDEN_FILTER);
        }
        return icon;
    }
}
