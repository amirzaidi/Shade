package amirz.shade.icons;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Process;

import com.android.launcher3.AppInfo;
import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.BitmapInfo;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.util.ComponentKey;

import amirz.shade.icons.calendar.DateChangeReceiver;
import amirz.shade.icons.calendar.DynamicCalendar;
import amirz.shade.icons.clock.CustomClock;
import amirz.shade.icons.clock.DynamicClock;
import amirz.shade.icons.pack.IconPackManager;
import amirz.shade.icons.pack.IconResolver;

import static com.android.launcher3.LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;

public class ThirdPartyDrawableFactory extends DrawableFactory {
    private final IconPackManager mManager;
    private final DynamicClock mDynamicClockDrawer;
    private final CustomClock mCustomClockDrawer;
    private final DateChangeReceiver mCalendars;

    public ThirdPartyDrawableFactory(Context context) {
        mManager = IconPackManager.get(context);
        if (Utilities.ATLEAST_OREO) {
            mDynamicClockDrawer = new DynamicClock(context);
            mCustomClockDrawer = new CustomClock(context);
        } else {
            mDynamicClockDrawer = null;
            mCustomClockDrawer = null;
        }
        mCalendars = new DateChangeReceiver(context);
    }

    @Override
    public final FastBitmapDrawable newIcon(ItemInfoWithIcon info) {
        if (info != null && info.getTargetComponent() != null
                && info.itemType == ITEM_TYPE_APPLICATION) {
            ComponentKey key = new ComponentKey(info.getTargetComponent(), info.user);

            IconResolver resolver = mManager.resolve(key);
            mCalendars.setCalendar(key, (resolver != null && resolver.isCalendar())
                || info.getTargetComponent().getPackageName().equals(DynamicCalendar.CALENDAR));

            if (Utilities.ATLEAST_OREO) {
                if (resolver != null) {
                    if (resolver.isClock()) {
                        Drawable drawable = resolver.getIcon(0, () -> null);
                        if (drawable != null) {
                            FastBitmapDrawable fb = mCustomClockDrawer.drawIcon(
                                    info, drawable, resolver.clockData());
                            fb.setIsDisabled(info.isDisabled());
                            return fb;
                        }
                    }
                } else if (info.getTargetComponent().equals(DynamicClock.DESK_CLOCK)) {
                    return mDynamicClockDrawer.drawIcon(info);
                }
            }
        }

        return super.newIcon(info);
    }

    @Override
    public FastBitmapDrawable newIcon(BitmapInfo info, ActivityInfo target) {
        AppInfo appInfo = new AppInfo();
        appInfo.iconBitmap = info.icon;
        appInfo.iconColor = info.color;
        appInfo.intent = new Intent().setComponent(
                new ComponentName(target.packageName, target.name));
        appInfo.user = Process.myUserHandle();
        return newIcon(appInfo);
    }
}
