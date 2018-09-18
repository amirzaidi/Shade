package amirz.shade.icons.pack;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class IconResolver {
    private PackageManager mPm;
    private ApplicationInfo mAppInfo;
    private int mDrawableId;

    IconResolver() {
    }

    IconResolver(PackageManager pm, ApplicationInfo appInfo, int drawableId) {
        mPm = pm;
        mAppInfo = appInfo;
        mDrawableId = drawableId;
    }

    public boolean useDefault() {
        return mPm == null;
    }

    public Drawable getIcon(int iconDpi) {
        try {
            Resources res = mPm.getResourcesForApplication(mAppInfo);
            Drawable drawable = res.getDrawableForDensity(mDrawableId, iconDpi, null);
            if (drawable != null) {
                return drawable;
            }
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException ignored) {
        }

        return mPm.getDrawable(mAppInfo.packageName, mDrawableId, mAppInfo);
    }
}
