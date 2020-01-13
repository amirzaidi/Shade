package amirz.shade.customization;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AppWidgetManagerCompat;

import java.util.ArrayList;
import java.util.List;

public class DockSearch {
    public static final String KEY_DOCK_SEARCH = "pref_dock_search";

    public static AppWidgetProviderInfo getWidgetInfo(Context context) {
        SharedPreferences prefs = Utilities.getPrefs(context);
        String val = prefs.getString(KEY_DOCK_SEARCH, "");
        for (AppWidgetProviderInfo info : validWidgets(context)) {
            if (val.equals(info.provider.flattenToShortString())) {
                return info;
            }
        }
        return null;
    }

    public static List<AppWidgetProviderInfo> validWidgets(Context context) {
        int highestMinHeight = context.getResources()
                .getDimensionPixelSize(R.dimen.qsb_wrapper_height);
        List<AppWidgetProviderInfo> widgets = new ArrayList<>();
        AppWidgetManagerCompat widgetManager = AppWidgetManagerCompat.getInstance(context);
        for (AppWidgetProviderInfo widgetInfo : widgetManager.getAllProviders(null)) {
            if (widgetInfo.resizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL
                    && widgetInfo.configure == null
                    && Math.min(widgetInfo.minHeight, widgetInfo.minResizeHeight) <= highestMinHeight) {
                widgets.add(widgetInfo);
            }
        }
        return widgets;
    }
}
