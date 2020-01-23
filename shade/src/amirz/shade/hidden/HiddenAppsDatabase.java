package amirz.shade.hidden;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.UserHandle;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.ComponentKey;

import java.util.Collections;
import java.util.Set;

public class HiddenAppsDatabase {
    private static final String KEY_HIDDEN = "pref_hidden_apps";

    public static boolean isHidden(Context context, ComponentName app, UserHandle user) {
        Set<String> hiddenSet = Utilities.getPrefs(context)
                .getStringSet(KEY_HIDDEN, Collections.emptySet());
        return hiddenSet.contains(new ComponentKey(app, user).toString());
    }

    public static void setHidden(Context context, ComponentName app, UserHandle user,
                                 boolean hidden) {
        SharedPreferences prefs = Utilities.getPrefs(context);
        Set<String> hiddenSet = prefs.getStringSet(KEY_HIDDEN, Collections.emptySet());
        String key = new ComponentKey(app, user).toString();
        if (hidden) {
            hiddenSet.add(key);
        } else {
            hiddenSet.remove(key);
        }
        prefs.edit().putStringSet(KEY_HIDDEN, hiddenSet).apply();
    }
}
