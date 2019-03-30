package amirz.shade.icons;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.android.launcher3.FastBitmapDrawable;
import com.android.launcher3.ItemInfoWithIcon;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.DrawableFactory;
import com.android.launcher3.util.ComponentKey;

import amirz.shade.icons.clock.CustomClock;
import amirz.shade.icons.pack.IconPackManager;
import amirz.shade.icons.pack.IconResolver;

import static com.android.launcher3.LauncherSettings.BaseLauncherColumns.ITEM_TYPE_APPLICATION;

public class ThirdPartyDrawableFactory extends DrawableFactory {
    private final IconPackManager mManager;
    private final CustomClock mCustomClockDrawer;

    public ThirdPartyDrawableFactory(Context context) {
        mManager = IconPackManager.get(context);
        mCustomClockDrawer = new CustomClock(context);
    }

    @Override
    public final FastBitmapDrawable newIcon(ItemInfoWithIcon info) {
        if (info != null && info.getTargetComponent() != null
                && info.itemType == ITEM_TYPE_APPLICATION && Utilities.ATLEAST_OREO) {
            ComponentKey key = new ComponentKey(info.getTargetComponent(), info.user);
            IconResolver resolver = mManager.resolve(key);

            if (resolver != null && resolver.isClock()) {
                Drawable drawable = resolver.getIconScaleInvariant();
                if (drawable != null){
                    FastBitmapDrawable fb = mCustomClockDrawer.drawIcon(
                            info, drawable, resolver.clockData());
                    fb.setIsDisabled(info.isDisabled());
                    return fb;
                }
            }
        }

        return super.newIcon(info);
    }
}
