package amirz.shade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.android.launcher3.Utilities;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ShadeFont {
    public static final String KEY_FONT = "pref_font";
    public static final String DEFAULT_FONT = "google_sans";

    private static Map<String, Typeface> sDeviceMap;

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("InflateParams")
    public static void override(Context context) {
        String font = getFont(context);

        try {
            final Field staticField = Typeface.class.getDeclaredField("sSystemFontMap");
            staticField.setAccessible(true);
            if (sDeviceMap == null) {
                //noinspection unchecked
                sDeviceMap = (Map<String, Typeface>) staticField.get(null);
            }

            if (TextUtils.isEmpty(font)) {
                // Disabled in Home settings.
                staticField.set(null, sDeviceMap);
                return;
            }

            Map<String, Typeface> newMap = new HashMap<>(sDeviceMap);

            AssetManager assets = context.getAssets();
            Typeface regular = Typeface.createFromAsset(assets, font + "_regular.ttf");
            Typeface medium = Typeface.createFromAsset(assets, font + "_medium.ttf");
            Typeface bold = Typeface.createFromAsset(assets, font + "_bold.ttf");

            newMap.put("sans-serif", regular);
            newMap.put("sans-serif-medium", medium);
            newMap.put("sans-serif-bold", bold);
            staticField.set(null, newMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static Typeface getTypeface(Context context) {
        String font = getFont(context);
        return TextUtils.isEmpty(font)
                ? null
                : Typeface.createFromAsset(context.getAssets(), font + "_regular.ttf");
    }

    public static String getFont(Context context) {
        return Utilities.getPrefs(context).getString(KEY_FONT, DEFAULT_FONT);
    }
}
