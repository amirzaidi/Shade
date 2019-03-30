package amirz.shade.icons.pack;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.util.Calendar;

import amirz.shade.icons.clock.CustomClock;

public class IconResolver {
    private final PackageManager mPm;
    private final ApplicationInfo mPackInfo;
    private final int mDrawableId;
    private final String mCalendarPrefix;
    private final IconPack.Clock mClockData;

    IconResolver(PackageManager pm, ApplicationInfo packInfo, int drawableId,
                 String calendarPrefix, IconPack.Clock clockData) {
        mPm = pm;
        mPackInfo = packInfo;
        mDrawableId = drawableId;
        mCalendarPrefix = calendarPrefix;
        mClockData = clockData;
    }

    public boolean isCalendar() {
        return mCalendarPrefix != null;
    }

    public boolean isClock() {
        return mClockData != null;
    }

    public CustomClock.Metadata clockData() {
        return new CustomClock.Metadata(
                mClockData.hourLayerIndex,
                mClockData.minuteLayerIndex,
                mClockData.secondLayerIndex,
                mClockData.defaultHour,
                mClockData.defaultMinute,
                mClockData.defaultSecond
        );
    }

    public Drawable getIcon(int iconDpi) {
        try {
            Resources res = mPm.getResourcesForApplication(mPackInfo);

            // First try loading the calendar.
            if (isCalendar()) {
                String calendarId = mCalendarPrefix + Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                int drawableId = res.getIdentifier(calendarId, "drawable", mPackInfo.packageName);
                if (drawableId != 0) {
                    Drawable drawable = mPm.getDrawable(mPackInfo.packageName, drawableId, null);
                    if (drawable != null) {
                        return drawable;
                    }
                }
            }

            // Fall back to mipmap loading with correct density.
            Drawable drawable = res.getDrawableForDensity(mDrawableId, iconDpi, null);
            if (drawable != null) {
                return drawable;
            }
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException ignored) {
        }

        // Finally, try directly returning the drawable.
        return getIconScaleInvariant();
    }

    public Drawable getIconScaleInvariant() {
        return mPm.getDrawable(mPackInfo.packageName, mDrawableId, mPackInfo);
    }
}
