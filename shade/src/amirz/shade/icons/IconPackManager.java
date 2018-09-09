package amirz.shade.icons;

import android.content.Context;
import android.content.pm.PackageManager;

public class IconPackManager {
    private static final Object sInstanceLock = new Object();
    private static IconPackManager sInstance;

    public static IconPackManager getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                sInstance = new IconPackManager(context);
            }
        }
        return sInstance;
    }

    private final Context mContext;

    private IconPackManager(Context context) {
        mContext = context;
    }

    public class Pack {
        private Pack(PackageManager pm) {

        }
    }
}
