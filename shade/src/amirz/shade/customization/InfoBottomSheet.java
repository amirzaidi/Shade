package amirz.shade.customization;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
import com.android.launcher3.compat.LauncherAppsCompat;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.widget.WidgetsBottomSheet;

import amirz.shade.settings.IconPackPrefSetter;
import amirz.shade.settings.ReloadingListPreference;
import amirz.shade.util.AppReloader;

import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.THREAD_POOL_EXECUTOR;

public class InfoBottomSheet extends WidgetsBottomSheet {
    private static final String CLIP_LABEL = "ComponentName";

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
        title.setOnLongClickListener(p -> {
            ClipboardManager cm = getContext().getSystemService(ClipboardManager.class);
            if (cm != null) {
                String str = itemInfo.getTargetComponent().flattenToString();
                cm.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, str));
            }
            return false;
        });

        // Use a proxy so we can update the reference at runtime.
        View.OnClickListener l = v -> mOnAppInfoClick.onClick(v);

        PrefsFragment fragment =
                (PrefsFragment) mFragmentManager.findFragmentById(R.id.sheet_prefs);
        fragment.loadForApp(itemInfo, l, () -> handleClose(true));
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
        private Runnable mAnimatedClose;

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

        @Override
        public void onResume() {
            super.onResume();
            if (mAnimatedClose != null && !isEnabled()) {
                mAnimatedClose.run();
            }
        }

        private boolean isEnabled() {
            if (mKey != null) {
                try {
                    return LauncherAppsCompat.getInstance(Launcher.getLauncher(mContext))
                            .isActivityEnabledForProfile(mKey.componentName, mKey.user);
                } catch (Exception ignored) {
                }
            }
            return false;
        }

        public void loadForApp(ItemInfo itemInfo, final View.OnClickListener onMoreClick,
                               final Runnable animatedClose) {
            mComponent = itemInfo.getTargetComponent();
            mKey = new ComponentKey(mComponent, itemInfo.user);
            mOnMoreClick = onMoreClick;
            mAnimatedClose = animatedClose;

            ReloadingListPreference icons = (ReloadingListPreference) findPreference(KEY_ICON_PACK);
            icons.setValue(IconDatabase.getByComponent(mContext, mKey));
            icons.setOnReloadListener(ctx -> new IconPackPrefSetter(ctx, mComponent));
            icons.setOnPreferenceChangeListener(this);

            THREAD_POOL_EXECUTOR.execute(() -> {
                MetadataExtractor extractor = new MetadataExtractor(mContext, mComponent);

                CharSequence source = extractor.getSource();
                CharSequence lastUpdate = extractor.getLastUpdate();
                CharSequence version = mContext.getString(
                        R.string.app_info_version_value,
                        extractor.getVersionName(),
                        extractor.getVersionCode());
                Intent marketIntent = extractor.getMarketIntent();

                MAIN_EXECUTOR.execute(() -> {
                    Preference sourcePref = findPreference(KEY_SOURCE);
                    Preference lastUpdatePref = findPreference(KEY_LAST_UPDATE);
                    Preference versionPref = findPreference(KEY_VERSION);
                    Preference morePref = findPreference(KEY_MORE);

                    sourcePref.setSummary(source);
                    lastUpdatePref.setSummary(lastUpdate);
                    versionPref.setSummary(version);
                    morePref.setOnPreferenceClickListener(this);

                    if (marketIntent != null) {
                        sourcePref.setOnPreferenceClickListener(
                                pref -> tryStartActivity(marketIntent));
                    }
                });
            });
        }

        private boolean tryStartActivity(Intent intent) {
            Launcher launcher = Launcher.getLauncher(mContext);
            Bundle opts = launcher.getAppTransitionManager()
                    .getActivityLaunchOptions(launcher, getView())
                    .toBundle();
            try {
                launcher.startActivity(intent, opts);
            } catch (Exception ignored) {
            }
            return false;
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
