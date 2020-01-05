package amirz.shade;

import android.os.Bundle;

import com.android.launcher3.settings.SettingsActivity;

public class ShadeSettings extends SettingsActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        super.onCreate(savedInstanceState);
    }
}
