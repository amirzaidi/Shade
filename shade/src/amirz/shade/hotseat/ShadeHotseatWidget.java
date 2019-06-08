package amirz.shade.hotseat;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsRecyclerView;
import com.android.launcher3.qsb.QsbContainerView;

import static amirz.shade.ShadeSettings.PREF_DOCK_SEARCH;
import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.android.launcher3.graphics.IconNormalizer.ICON_VISIBLE_AREA_FACTOR;

public class ShadeHotseatWidget extends QsbContainerView implements Insettable {
    private final Launcher mLauncher;

    public ShadeHotseatWidget(Context context) {
        this(context, null);
    }

    public ShadeHotseatWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShadeHotseatWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mLauncher = Launcher.getLauncher(context);
    }

    @Override
    public void setInsets(Rect rect) {
        DeviceProfile dp = mLauncher.getDeviceProfile();
        setTranslationY((float) -rect.bottom);
        setVisibility(dp.isVerticalBarLayout() ? View.GONE : View.VISIBLE);
        getLayoutParams().height = dp.hotseatBarSizePx;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Update the width to match the grid padding
        DeviceProfile dp = mLauncher.getDeviceProfile();
        int myRequestedWidth = getSize(widthMeasureSpec);

        AllAppsRecyclerView rv = mLauncher.getAppsView().getActiveRecyclerView();
        int rowWidth = myRequestedWidth - rv.getPaddingLeft() - rv.getPaddingRight();

        int cellWidth = DeviceProfile.calculateCellWidth(rowWidth, dp.inv.numHotseatIcons);
        int iconVisibleSize = Math.round(ICON_VISIBLE_AREA_FACTOR * dp.iconSizePx);
        int iconPadding = cellWidth - iconVisibleSize;

        int myWidth = rowWidth - iconPadding;
        super.onMeasure(makeMeasureSpec(myWidth, EXACTLY), heightMeasureSpec);
    }

    public static class HotseatFragment extends QsbFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {
        private SharedPreferences mPrefs;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mPrefs = Utilities.getPrefs(getActivity());
            mPrefs.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            mPrefs.unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean isQsbEnabled() {
            return ShadeHotseat.hasWidget(getActivity());
        }

        @Override
        protected AppWidgetProviderInfo getSearchWidgetProvider() {
            String widget = mPrefs.getString(PREF_DOCK_SEARCH, "");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
            for (AppWidgetProviderInfo info : appWidgetManager.getInstalledProviders()) {
                if (widget.equals(info.provider.flattenToShortString())) {
                    return info;
                }
            }
            return null;
        }

        @Override
        protected QsbWidgetHost createHost() {
            String widget = Utilities.getPrefs(getActivity()).getString(PREF_DOCK_SEARCH, "");
            return new QsbWidgetHost(getActivity(),
                    QsbFragment.QSB_WIDGET_HOST_ID ^ widget.hashCode(),
                    HotseatWidgetHostView::new);
        }

        @Override
        protected View getDefaultView(ViewGroup container, boolean showSetupIcon) {
            View v = super.getDefaultView(container, showSetupIcon);
            if (showSetupIcon) {
                v.findViewById(R.id.btn_qsb_setup).performClick();
            }
            return v;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PREF_DOCK_SEARCH.equals(key)) {
                onPause();
                onResume();
            }
        }
    }
}
