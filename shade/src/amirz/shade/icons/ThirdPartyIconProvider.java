package amirz.shade.icons;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;

import amirz.shade.icons.calendar.DynamicCalendar;
import amirz.shade.icons.clock.CustomClock;
import amirz.shade.icons.clock.DynamicClock;
import amirz.shade.icons.pack.IconPackManager;
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

        Drawable icon = getByKey(mContext, key, iconDpi);
        return icon == null
                ? AdaptiveIconWrapper.getInstance(mContext).wrap(key,
                        super.getIcon(launcherActivityInfo, iconDpi, flattenDrawable))
                : icon;
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
