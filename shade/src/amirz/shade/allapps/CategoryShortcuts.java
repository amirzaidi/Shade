package amirz.shade.allapps;

import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Process;

import com.android.launcher3.AppInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.LauncherIcons;

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
        for (int i = 0; i < titles.length; i++) {
            Drawable d = res.getDrawable(icons.getResourceId(i, 0), launcher.getTheme());
            Bitmap bm;
            if (Utilities.ATLEAST_OREO) {
                bm = launcherIcons.createBadgedIconBitmap(d, Process.myUserHandle(),
                        Build.VERSION.SDK_INT, false, null).icon;
            } else {
                LayerDrawable circle = (LayerDrawable) res.getDrawable(
                        R.drawable.ic_category_plate, launcher.getTheme());
                circle.setDrawableByLayerId(R.id.ic_category_plate_icon, d);
                bm = launcherIcons.createBadgedIconBitmap(circle, Process.myUserHandle(),
                        Build.VERSION.SDK_INT, false, null).icon;
            }

            AppInfo ai = new AppInfo();
            ai.componentName = cn;
            ai.intent = new Intent().setComponent(cn).putExtra("search", titles[i]);
            ai.iconBitmap = bm;
            ai.iconColor = 0;
            ai.title = titles[i];

            categories.add(ai);
        }
        icons.recycle();

        return categories;
    }
}
