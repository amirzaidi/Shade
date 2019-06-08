package amirz.shade.hotseat;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.android.launcher3.CellLayout;
import com.android.launcher3.Hotseat;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.compat.AppWidgetManagerCompat;

import java.util.ArrayList;
import java.util.List;

import static amirz.shade.ShadeSettings.PREF_DOCK_SEARCH;

public class ShadeHotseat extends Hotseat {
    private final Launcher mLauncher;

    public ShadeHotseat(Context context) {
        this(context, null);
    }

    public ShadeHotseat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadeHotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mLauncher = Launcher.getLauncher(context);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        setVisibility();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void setInsets(Rect insets) {
        super.setInsets(insets);
        setVisibility();
    }

    private void setVisibility() {
        boolean widgetVisible = hasWidget(mLauncher);

        findViewById(R.id.search_container_hotseat).setAlpha(widgetVisible ? 1f : 0.1f);
        CellLayout cell = getLayout();
        for (int i = 0; i < cell.getChildCount(); i++) {
            cell.getChildAt(i).setAlpha(widgetVisible ? 0f : 1f);
        }
    }

    @Override
    public boolean pointInBounds(int[] xy) {
        return !hasWidget(mLauncher) && super.pointInBounds(xy);
    }

    public static boolean hasWidget(Context context) {
        Launcher launcher = Launcher.getLauncher(context);
        if (launcher.getDeviceProfile().isVerticalBarLayout()) {
            return false;
        }

        SharedPreferences prefs = Utilities.getPrefs(context);
        String val = prefs.getString(PREF_DOCK_SEARCH, "");
        for (AppWidgetProviderInfo info : validWidgets(context)) {
            if (val.equals(info.provider.flattenToShortString())) {
                return true;
            }
        }
        return false;
    }

    public static List<AppWidgetProviderInfo> validWidgets(Context context) {
        int minHeight = context.getResources().getDimensionPixelSize(R.dimen.hotseat_qsb_size);
        List<AppWidgetProviderInfo> widgets = new ArrayList<>();
        AppWidgetManagerCompat widgetManager = AppWidgetManagerCompat.getInstance(context);
        for (AppWidgetProviderInfo widgetInfo : widgetManager.getAllProviders(null)) {
            if (widgetInfo.resizeMode == AppWidgetProviderInfo.RESIZE_HORIZONTAL
                    && widgetInfo.configure == null
                    && Math.min(widgetInfo.minHeight, widgetInfo.minResizeHeight) <= minHeight) {
                widgets.add(widgetInfo);
            }
        }
        return widgets;
    }
}
