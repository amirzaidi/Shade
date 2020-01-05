package amirz.shade;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.launcher3.settings.SettingsActivity;

public class ShadeSettings extends SettingsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        super.onCreate(savedInstanceState);
    }

    public static class ShadeSettingsFragment extends LauncherSettingsFragment {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            PreferenceScreen screen = getPreferenceScreen();
            for (int i = 0; i < screen.getPreferenceCount(); i++) {
                Preference preference = screen.getPreference(i);
                preference.setIconSpaceReserved(false);
            }
        }
    }
}
