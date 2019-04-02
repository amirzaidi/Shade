package amirz.shade;

import android.content.Context;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import com.android.launcher3.SettingsActivity;
import com.android.launcher3.Utilities;
import com.android.launcher3.uioverrides.WallpaperColorInfo;

import amirz.shade.customization.GlobalIconPackPreference;
import amirz.shade.customization.AppReloader;

public class ShadeSettings extends SettingsActivity {
    public static final String PREF_THEME = "pref_theme";
    public static final String PREF_ICON_PACK = "pref_icon_pack";

    @Override
    protected PreferenceFragment getNewFragment() {
        return new ShadeFragment();
    }

    public static class ShadeFragment extends SettingsActivity.LauncherSettingsFragment {
        private Context mContext;
        private GlobalIconPackPreference mIconPackPref;
        private ListPreference mThemePref;

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            mContext = getActivity();

            mIconPackPref = (GlobalIconPackPreference) findPreference(PREF_ICON_PACK);
            mIconPackPref.setOnPreferenceChangeListener((p, v) -> {
                Utilities.getPrefs(mContext).edit().putString(PREF_ICON_PACK, v.toString()).apply();
                AppReloader.get(mContext).reload();
                return true;
            });

            mThemePref = (ListPreference) findPreference(PREF_THEME);
            mThemePref.setOnPreferenceChangeListener((p, v) -> {
                Utilities.getPrefs(mContext).edit().putString(PREF_THEME, v.toString()).apply();
                WallpaperColorInfo.getInstance(mContext).notifyChange();
                return true;
            });
        }
    }
}
