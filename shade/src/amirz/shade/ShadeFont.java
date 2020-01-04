package amirz.shade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ShadeFont {
    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint("InflateParams")
    public static void override(Context context) {
        AssetManager assets = context.getAssets();
        Typeface regular = Typeface.createFromAsset(assets, "google_sans_regular.ttf");
        Typeface medium = Typeface.createFromAsset(assets, "google_sans_medium.ttf");
        Typeface bold = Typeface.createFromAsset(assets, "google_sans_bold.ttf");

        try {
            final Field staticField = Typeface.class.getDeclaredField("sSystemFontMap");
            staticField.setAccessible(true);
            Map<String, Typeface> unmodifiableMap = (Map<String, Typeface>) staticField.get(null);
            Map<String, Typeface> newMap = new HashMap<>(unmodifiableMap);
            newMap.put("sans-serif", regular);
            newMap.put("sans-serif-medium", medium);
            newMap.put("sans-serif-bold", bold);
            staticField.set(null, newMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
