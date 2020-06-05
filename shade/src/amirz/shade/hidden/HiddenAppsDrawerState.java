package amirz.shade.hidden;

import android.content.Context;

import com.android.launcher3.util.Executors;

import amirz.shade.util.AppReloader;

import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;

public class HiddenAppsDrawerState {
    private static HiddenAppsDrawerState sInstance;

    public static synchronized HiddenAppsDrawerState getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HiddenAppsDrawerState(context);
        }
        return sInstance;
    }

    private final Context mContext;
    private boolean mRevealed;

    private HiddenAppsDrawerState(Context context) {
        mContext = context;
    }

    public void toggleRevealed() {
        setRevealed(!isRevealed());
    }

    public void setRevealed(boolean revealed) {
        if (mRevealed != revealed) {
            mRevealed = revealed;
            MODEL_EXECUTOR.execute(() -> {
                AppReloader reloader = AppReloader.get(mContext);
                reloader.reload(reloader.hiddenApps());
            });
        }
    }

    public boolean isRevealed() {
        return mRevealed;
    }
}
