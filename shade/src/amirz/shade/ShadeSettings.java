package amirz.shade;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.android.launcher3.SettingsActivity;

import amirz.shade.customization.GlobalIconPackPreference;
import amirz.shade.icons.pack.IconReloader;

public class ShadeSettings extends SettingsActivity {
    public static final String PREF_ICON_PACK = "pref_icon_pack";

    @Override
    protected PreferenceFragment getNewFragment() {
        return new ShadeFragment();
    }

    public static class ShadeFragment extends SettingsActivity.LauncherSettingsFragment {
        private Context mContext;
        private GlobalIconPackPreference mIconPackPref;

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            mContext = getActivity();

            mIconPackPref = (GlobalIconPackPreference) findPreference(PREF_ICON_PACK);
            mIconPackPref.setOnPreferenceChangeListener((p, v) -> {
                IconReloader.get(mContext).reload();
                return true;
            });
        }
    }
}
