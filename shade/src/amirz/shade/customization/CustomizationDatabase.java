package amirz.shade.customization;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.util.ComponentKey;

public class CustomizationDatabase {
    private static final String APP_ICON_PACK = BuildConfig.APPLICATION_ID + ".APP_ICON_PACK";
    private static final String APP_CATEGORY = BuildConfig.APPLICATION_ID + ".CATEGORY";

    // Icon pack
    public static String getIconPack(Context context, ComponentKey key) {
        String global = GlobalIconPackPreference.get(context);
        return getIconPackPrefs(context).getString(key.toString(), global);
    }

    public static void setIconPack(Context context, ComponentKey key, String value) {
        getIconPackPrefs(context).edit().putString(key.toString(), value).apply();
    }

    public static void clearIconPack(Context context, ComponentKey key) {
        getIconPackPrefs(context).edit().remove(key.toString()).apply();
    }

    private static SharedPreferences getIconPackPrefs(Context context) {
        return context.getSharedPreferences(APP_ICON_PACK, Context.MODE_PRIVATE);
    }

    // Category
    public static String getCategory(Context context, ComponentKey key) {
        String category = getCategoryPrefs(context).getString(key.toString(), "");

        // Use automatic categorization for unknown categories
        if (category.isEmpty()) {
            category = AutoCategorize.getCategory(context, key.componentName);
            setCategory(context, key, category);
        }

        return category;
    }

    public static void setCategory(Context context, ComponentKey key, String value) {
        getCategoryPrefs(context).edit().putString(key.toString(), value).apply();
    }

    public static void clearCategory(Context context, ComponentKey key) {
        getCategoryPrefs(context).edit().remove(key.toString()).apply();
    }

    private static SharedPreferences getCategoryPrefs(Context context) {
        return context.getSharedPreferences(APP_CATEGORY, Context.MODE_PRIVATE);
    }
}
