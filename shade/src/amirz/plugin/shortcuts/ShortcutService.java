package amirz.plugin.shortcuts;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.android.launcher3.plugin.shortcuts.IShortcutPlugin;

import java.util.List;

public class ShortcutService extends Service {
    private static final String TAG = "ShortcutService";

    private ShortcutExtractor mExtractor;
    private IBinder mSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mExtractor = new ShortcutExtractor(this);
        mSession = new IShortcutPlugin.Stub() {
            @Override
            public List<Bundle> queryShortcuts(String packageName, ComponentName activity) {
                return mExtractor.getForActivity(packageName, activity);
            }

            @Override
            public Bitmap getIcon(String key, int density) {
                return mExtractor.getIcon(key, density);
            }
        };
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mSession;
    }
}
