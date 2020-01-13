package amirz.shade;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.settings.SettingsActivity;

import amirz.shade.customization.IconDatabase;
import amirz.shade.settings.DockSearchPrefSetter;
import amirz.shade.settings.FeedProviderPrefSetter;
import amirz.shade.settings.IconPackPrefSetter;
import amirz.shade.settings.ReloadingListPreference;
import amirz.shade.util.AppReloader;

import static amirz.shade.ShadeLauncherCallbacks.KEY_ENABLE_MINUS_ONE;
import static amirz.shade.ShadeLauncherCallbacks.KEY_FEED_PROVIDER;
import static amirz.shade.customization.DockSearch.KEY_DOCK_SEARCH;

public class ShadeSettings extends SettingsActivity {
    public interface OnResumePreferenceCallback {
        void onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    public static class ShadeSettingsFragment extends LauncherSettingsFragment {
        private static final String KEY_ICON_PACK = "pref_icon_pack";
        private static final String KEY_APP_VERSION = "pref_app_version";

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            final Context context = getActivity();

            // Customization
            ReloadingListPreference icons = (ReloadingListPreference) findPreference(KEY_ICON_PACK);
            icons.setOnReloadListener(new IconPackPrefSetter(context));
            icons.setOnPreferenceChangeListener((pref, val) -> {
                IconDatabase.clearAll(context);
                IconDatabase.setGlobal(context, (String) val);
                AppReloader.get(context).reload();
                return true;
            });

            ReloadingListPreference search =
                    (ReloadingListPreference) findPreference(KEY_DOCK_SEARCH);
            search.setOnReloadListener(new DockSearchPrefSetter(context));

            ReloadingListPreference feed =
                    (ReloadingListPreference) findPreference(KEY_FEED_PROVIDER);
            feed.setOnReloadListener(new FeedProviderPrefSetter(context));
            feed.setOnPreferenceChangeListener((pref, val) -> {
                Utilities.getPrefs(context).edit()
                        .putBoolean(KEY_ENABLE_MINUS_ONE, !TextUtils.isEmpty((String) val))
                        .apply();
                return true;
            });

            // About
            String versionName = BuildConfig.VERSION_NAME;
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo pi = pm.getPackageInfo(BuildConfig.APPLICATION_ID, 0);
                versionName = pi.versionName;
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            Preference version = findPreference(KEY_APP_VERSION);
            version.setSummary(context.getString(R.string.about_app_version_value,
                    versionName, BuildConfig.BUILD_TYPE));
            Uri intentData = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            version.setIntent(version.getIntent().setData(intentData));
        }

        @Override
        public void onResume() {
            super.onResume();

            PreferenceScreen screen = getPreferenceScreen();
            for (int i = 0; i < screen.getPreferenceCount(); i++) {
                Preference preference = screen.getPreference(i);
                if (preference instanceof OnResumePreferenceCallback) {
                    ((OnResumePreferenceCallback) preference).onResume();
                }
            }
        }
    }
}
