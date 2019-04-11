package amirz.shade.customization;

import android.content.Context;
import android.util.AttributeSet;

import com.android.launcher3.SettingsActivity;
import com.android.launcher3.Utilities;

import java.util.Map;

import amirz.shade.ShadeSettings;
import amirz.shade.icons.pack.IconPackManager;

public class GlobalIconPackPreference extends IconPackPreference {
    public GlobalIconPackPreference(Context context) {
        super(context);
    }

    public GlobalIconPackPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GlobalIconPackPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public GlobalIconPackPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected Map<String, CharSequence> getPacks() {
        return IconPackManager.get(getContext()).getProviderNames();
    }

    public static String get(Context context) {
        return Utilities.getPrefs(context).getString(ShadeSettings.PREF_ICON_PACK, "");
    }

    public static void reset(Context context) {
        Utilities.getPrefs(context).edit().remove(ShadeSettings.PREF_ICON_PACK).apply();
    }
}
