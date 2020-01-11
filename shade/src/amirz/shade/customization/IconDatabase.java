package amirz.shade.customization;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.util.ComponentKey;

public class IconDatabase {
    private static final String PREF_FILE_NAME = BuildConfig.APPLICATION_ID + ".ICON_DATABASE";
    private static final String KEY_GLOBAL = "global";
    private static final String VALUE_DEFAULT = "";

    public static String getGlobal(Context context) {
        return getIconPackPrefs(context).getString(KEY_GLOBAL, VALUE_DEFAULT);
    }

    public static void setGlobal(Context context, String value) {
        getIconPackPrefs(context).edit().putString(KEY_GLOBAL, value).apply();
    }

    public static void resetGlobal(Context context) {
        getIconPackPrefs(context).edit().remove(KEY_GLOBAL).apply();
    }

    public static String getByComponent(Context context, ComponentKey key) {
        return getIconPackPrefs(context).getString(key.toString(), getGlobal(context));
    }

    public static void setForComponent(Context context, ComponentKey key, String value) {
        getIconPackPrefs(context).edit().putString(key.toString(), value).apply();
    }

    public static void resetForComponent(Context context, ComponentKey key) {
        getIconPackPrefs(context).edit().remove(key.toString()).apply();
    }

    private static SharedPreferences getIconPackPrefs(Context context) {
        return context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }
}
