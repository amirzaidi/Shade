package amirz.shade.allapps;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Process;

import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.FixedScaleDrawable;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.util.Themes;

import java.util.ArrayList;
import java.util.List;

import amirz.shade.ShadeSearch;

class CategoryShortcuts {
    static List<AppInfo> getAll(Launcher launcher) {
        List<AppInfo> categories = new ArrayList<>();
        ComponentName cn = new ComponentName(launcher, ShadeSearch.class);
        LauncherIcons launcherIcons = LauncherIcons.obtain(launcher);

        Resources res = launcher.getResources();
        String[] titles = res.getStringArray(R.array.category_entries);
        TypedArray icons = res.obtainTypedArray(R.array.category_entry_icons);
        for (int i = 0; i < icons.length(); i++) {
            Drawable d = res.getDrawable(icons.getResourceId(i, 0), launcher.getTheme());
            Bitmap bm;
            if (Utilities.ATLEAST_OREO) {
                AdaptiveIconDrawable dr = (AdaptiveIconDrawable)
                        launcher.getDrawable(R.drawable.adaptive_icon_drawable_wrapper).mutate();
                dr.setBounds(0, 0, 1, 1);
                float scale = launcherIcons.getNormalizer().getScale(
                        dr, new RectF(), dr.getIconMask(), new boolean[1]);
                FixedScaleDrawable fsd = ((FixedScaleDrawable) dr.getForeground());
                fsd.setDrawable(d);
                fsd.setScale(scale);
                ((ColorDrawable) dr.getBackground()).setColor(
                        Themes.getAttrColor(launcher, R.attr.headerColor));
                d = dr;
            } else {
                LayerDrawable circle = (LayerDrawable) res.getDrawable(
                        R.drawable.ic_category_plate, launcher.getTheme());
                circle.setDrawableByLayerId(R.id.ic_category_plate_icon, d);
                d = circle;
            }

            bm = launcherIcons.createBadgedIconBitmap(d, Process.myUserHandle(),
                    Build.VERSION.SDK_INT, false, null).icon;

            AppInfo ai = new AppInfo();
            ai.componentName = cn;
            ai.intent = new Intent().setComponent(cn).putExtra("search", titles[i]);
            ai.iconBitmap = bm;
            ai.iconColor = 0;
            ai.title = titles[i];

            categories.add(ai);
        }
        icons.recycle();
        launcherIcons.recycle();

        return categories;
    }
}
