package amirz.shade.hidden;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

public class HiddenAppsDatabase {
    public static boolean isHidden(Context context, ComponentName app, UserHandle user) {
        return false;
    }
}
