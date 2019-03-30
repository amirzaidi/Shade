package amirz.shade.icons;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;

import amirz.shade.icons.calendar.DateChangeReceiver;
import amirz.shade.icons.calendar.DynamicCalendar;
import amirz.shade.icons.clock.CustomClock;
import amirz.shade.icons.clock.DynamicClock;
import amirz.shade.icons.pack.IconPackManager;
import amirz.shade.icons.pack.IconResolver;

public class ThirdPartyIconProvider extends RoundIconProvider {
    private final Context mContext;
    private final IconPackManager mManager;
    private final DateChangeReceiver mCalendars;

    public ThirdPartyIconProvider(Context context) {
        super(context);
        mContext = context;
        mManager = IconPackManager.get(context);
        mCalendars = new DateChangeReceiver(context);
    }

    @Override
    public Drawable getIcon(LauncherActivityInfo launcherActivityInfo, int iconDpi, boolean flattenDrawable) {
        ComponentKey key = new ComponentKey(
                launcherActivityInfo.getComponentName(), launcherActivityInfo.getUser());

        IconResolver resolver = mManager.resolve(key);
        mCalendars.setCalendar(key, resolver != null && resolver.isCalendar());

        Drawable icon = resolver == null
                ? null
                : resolver.getIcon(iconDpi);

        if (Utilities.ATLEAST_OREO) {
            // Icon pack clock found
            if (icon != null && resolver.isClock()) {
                return CustomClock.getClock(mContext, icon, resolver.clockData());
            }

            // Google Clock override
            if (icon == null && launcherActivityInfo.getComponentName()
                    .equals(DynamicClock.DESK_CLOCK)) {
                icon = DynamicClock.getClock(mContext, iconDpi);
            }
        }

        // Google Calendar override
        if (icon == null && launcherActivityInfo.getComponentName().getPackageName()
                .equals(DynamicCalendar.CALENDAR)) {
            mCalendars.setCalendar(key, true);
            icon = DynamicCalendar.load(mContext, launcherActivityInfo.getComponentName(), iconDpi);
        }

        return icon == null
                ? super.getIcon(launcherActivityInfo, iconDpi, flattenDrawable)
                : icon;
    }
}
