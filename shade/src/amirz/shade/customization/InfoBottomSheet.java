package amirz.shade.customization;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.widget.WidgetsBottomSheet;

import amirz.shade.settings.IconPackPrefSetter;
import amirz.shade.settings.ReloadingListPreference;
import amirz.shade.util.AppReloader;

public class InfoBottomSheet extends WidgetsBottomSheet {
    private final FragmentManager mFragmentManager;
    private View.OnClickListener mOnAppInfoClick;

    public InfoBottomSheet(Context context) {
        this(context, null);
    }

    public InfoBottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfoBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
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

        // Use a proxy so we can update the reference at runtime.
        View.OnClickListener l = v -> mOnAppInfoClick.onClick(v);

        PrefsFragment fragment =
                (PrefsFragment) mFragmentManager.findFragmentById(R.id.sheet_prefs);
        fragment.loadForApp(itemInfo, l);
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
    public void onWidgetsBound() {
    }

    public static class PrefsFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
        private static final String KEY_ICON_PACK = "pref_app_info_icon_pack";
        private static final String KEY_SOURCE = "pref_app_info_source";
        private static final String KEY_LAST_UPDATE = "pref_app_info_last_update";
        private static final String KEY_VERSION = "pref_app_info_version";
        private static final String KEY_MORE = "pref_app_info_more";

        private Context mContext;

        private ComponentName mComponent;
        private ComponentKey mKey;
        private View.OnClickListener mOnMoreClick;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mContext = getActivity();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.app_info_preferences);
        }

        @Override
        public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent,
                                                 Bundle savedInstanceState) {
            RecyclerView view = super.onCreateRecyclerView(inflater, parent, savedInstanceState);
            view.setOverScrollMode(View.OVER_SCROLL_NEVER);
            return view;
        }

        public void loadForApp(ItemInfo itemInfo, final View.OnClickListener onMoreClick) {
            mComponent = itemInfo.getTargetComponent();
            mKey = new ComponentKey(mComponent, itemInfo.user);
            mOnMoreClick = onMoreClick;

            MetadataExtractor extractor = new MetadataExtractor(mContext, mComponent);

            ReloadingListPreference icons = (ReloadingListPreference) findPreference(KEY_ICON_PACK);
            icons.setOnReloadListener(new IconPackPrefSetter(mContext, mComponent));
            icons.setOnPreferenceChangeListener(this);
            icons.setValue(IconDatabase.getByComponent(mContext, mKey));

            findPreference(KEY_SOURCE).setSummary(extractor.getSource());
            findPreference(KEY_LAST_UPDATE).setSummary(extractor.getLastUpdate());
            findPreference(KEY_VERSION).setSummary(mContext.getString(
                    R.string.app_info_version_value,
                    extractor.getVersionName(),
                    extractor.getVersionCode()));
            findPreference(KEY_MORE).setOnPreferenceClickListener(this);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if (newValue.equals(IconDatabase.getGlobal(mContext))) {
                IconDatabase.resetForComponent(mContext, mKey);
            } else {
                IconDatabase.setForComponent(mContext, mKey, (String) newValue);
            }
            AppReloader.get(mContext).reload(mKey);
            return true;
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            mOnMoreClick.onClick(getView());
            return false;
        }
    }
}
