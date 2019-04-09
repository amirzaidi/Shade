package amirz.shade.icons;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.FixedScaleDrawable;
import com.android.launcher3.graphics.IconNormalizer;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.util.ComponentKey;

import amirz.shade.icons.calendar.DynamicCalendar;
import amirz.shade.icons.clock.CustomClock;
import amirz.shade.icons.clock.DynamicClock;
import amirz.shade.icons.pack.IconPackManager;
import amirz.shade.icons.pack.IconResolver;

public class ThirdPartyIconProvider extends RoundIconProvider {
    private final Context mContext;
    private Drawable mWrapperIcon;

    public ThirdPartyIconProvider(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public Drawable getIcon(LauncherActivityInfo launcherActivityInfo, int iconDpi, boolean flattenDrawable) {
        ComponentKey key = new ComponentKey(
                launcherActivityInfo.getComponentName(), launcherActivityInfo.getUser());

        Drawable icon = getByKey(mContext, key, iconDpi);
        return icon == null
                ? wrapAdaptive(super.getIcon(launcherActivityInfo, iconDpi, flattenDrawable))
                : icon;
    }

    private Drawable wrapAdaptive(Drawable icon) {
        if (Utilities.ATLEAST_OREO && !(icon instanceof AdaptiveIconDrawable)) {
            boolean[] outShape = new boolean[1];
            if (mWrapperIcon == null) {
                mWrapperIcon = mContext.getDrawable(R.drawable.adaptive_icon_drawable_wrapper)
                        .mutate();
            }

            AdaptiveIconDrawable dr = (AdaptiveIconDrawable) mWrapperIcon;
            dr.setBounds(0, 0, 1, 1);

            LauncherIcons icons = LauncherIcons.obtain(mContext);
            IconNormalizer normalizer = icons.getNormalizer();
            float scale = normalizer.getScale(icon, null, dr.getIconMask(), outShape);
            icons.recycle();

            if (!outShape[0]) {
                FixedScaleDrawable fsd = ((FixedScaleDrawable) dr.getForeground());
                fsd.setDrawable(icon);
                fsd.setScale(scale);
                ((ColorDrawable) dr.getBackground()).setColor(Color.WHITE);
                return dr;
            }
        }
        return icon;
    }

    static Drawable getByKey(Context context, ComponentKey key, int iconDpi) {
        IconResolver resolver = IconPackManager.get(context).resolve(key);
        Drawable icon = resolver == null
                ? null
                : resolver.getIcon(iconDpi);

        if (Utilities.ATLEAST_OREO) {
            // Icon pack clock found
            if (icon != null && resolver.isClock()) {
                return CustomClock.getClock(context, icon, resolver.clockData());
            }

            // Google Clock override
            if (icon == null && key.componentName.equals(DynamicClock.DESK_CLOCK)) {
                return DynamicClock.getClock(context, iconDpi);
            }
        }

        // Google Calendar override
        if (icon == null && key.componentName.getPackageName().equals(DynamicCalendar.CALENDAR)) {
            return DynamicCalendar.load(context, key.componentName, iconDpi);
        }

        return icon;
    }
}
