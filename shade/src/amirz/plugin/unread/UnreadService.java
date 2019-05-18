package amirz.plugin.unread;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UnreadService extends Service {
    private static final String TAG = "UnreadService";

    private IBinder mSession;

    @Override
    public void onCreate() {
        super.onCreate();
        mSession = new UnreadSession(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        return mSession;
    }
}
