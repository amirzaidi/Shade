package amirz.shade.customization;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.util.ComponentKey;

import amirz.shade.allapps.search.AppsSearchContainerLayout;
import amirz.shade.widget.WidgetsSheet;

public class PreferencesBottomSheet extends WidgetsSheet {
    private final FragmentManager mFragmentManager;
    private View.OnClickListener mOnAppInfoClick;

    public PreferencesBottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferencesBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFragmentManager = Launcher.getLauncher(context).getFragmentManager();
    }

    public void setOnAppInfoClick(View.OnClickListener onclick) {
        mOnAppInfoClick = onclick;
    }

    @Override
    public void populateAndShow(ItemInfo itemInfo) {
        super.populateAndShow(itemInfo);
        TextView title = findViewById(R.id.title);
        title.setText(itemInfo.title);

        View.OnClickListener l = v -> mOnAppInfoClick.onClick(v);
        title.setOnClickListener(l);
        findViewById(R.id.subtitle).setOnClickListener(l);

        PrefsFragment fragment =
                (PrefsFragment) mFragmentManager.findFragmentById(R.id.sheet_prefs);
        fragment.loadForApp(itemInfo, v -> handleClose(true));
    }

    @Override
    public void onDetachedFromWindow() {
        Fragment pf = mFragmentManager.findFragmentById(R.id.sheet_prefs);
        if (pf != null) {
            mFragmentManager.beginTransaction()
                    .remove(pf)
                    .commitAllowingStateLoss();
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onWidgetsBound() {
    }

    public static class PrefsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
        private final static String PREF_DEBUG_PACKAGE = "pref_debug_package";
        private final static String PREF_DEBUG_ACTIVITY = "pref_debug_activity";

        private final static String PREF_ICON = "pref_app_icon";
        private final static String PREF_CATEGORY = "pref_app_category";
        private final static String PREF_RESET = "pref_app_reset";

        private Context mContext;

        private ComponentName mComponent;
        private ComponentKey mKey;

        private IconPackPreference mPrefIcon;
        private CategoryPreference mPrefCategory;
        private Preference mPrefReset;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mContext = getActivity();

            if (BuildConfig.DEBUG) {
                addPreferencesFromResource(R.xml.app_edit_prefs_debug);
            }
            addPreferencesFromResource(R.xml.app_edit_prefs);
        }

        public void loadForApp(ItemInfo itemInfo, final View.OnClickListener onResetClick) {
            mComponent = itemInfo.getTargetComponent();
            mKey = new ComponentKey(mComponent, itemInfo.user);

            if (BuildConfig.DEBUG) {
                ClipboardManager clipboard =
                        (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);

                Preference.OnPreferenceClickListener onClick = p -> {
                    ClipData data = ClipData.newPlainText("pref", mComponent.flattenToString());
                    clipboard.setPrimaryClip(data);
                    Toast.makeText(mContext, R.string.pref_debug_copy, Toast.LENGTH_SHORT).show();
                    return false;
                };

                Preference pkg = findPreference(PREF_DEBUG_PACKAGE);
                pkg.setSummary(mComponent.getPackageName());
                pkg.setOnPreferenceClickListener(onClick);

                Preference act = findPreference(PREF_DEBUG_ACTIVITY);
                act.setSummary(mComponent.getClassName());
                act.setOnPreferenceClickListener(onClick);
            }

            mPrefIcon = (IconPackPreference) findPreference(PREF_ICON);
            mPrefIcon.setValue(CustomizationDatabase.getIconPack(mContext, mKey));
            mPrefIcon.setOnPreferenceChangeListener(this);

            mPrefCategory = (CategoryPreference) findPreference(PREF_CATEGORY);
            mPrefCategory.setValue(CustomizationDatabase.getCategory(mContext, mKey));
            mPrefCategory.setOnPreferenceChangeListener(this);

            mPrefReset = findPreference(PREF_RESET);
            mPrefReset.setOnPreferenceClickListener(preference -> {
                CustomizationDatabase.clearIconPack(mContext, mKey);
                CustomizationDatabase.clearCategory(mContext, mKey);
                reloadApp();
                onResetClick.onClick(PrefsFragment.this.getView());
                return true;
            });
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case PREF_ICON:
                    CustomizationDatabase.setIconPack(mContext, mKey, (String) newValue);
                    reloadApp();
                    return true;
                case PREF_CATEGORY:
                    CustomizationDatabase.setCategory(mContext, mKey, (String) newValue);
                    reloadApp();
                    AppsSearchContainerLayout search = (AppsSearchContainerLayout)
                            Launcher.getLauncher(mContext).getAppsView().getSearchUiManager();

                    // Reset search text when the category has changed, to trigger search handlers.
                    search.setText(search.getText());
                    return true;
            }
            return false;
        }

        private void reloadApp() {
            // Call this on any change.
            AppReloader.get(mContext).reload(mKey);
        }
    }
}
