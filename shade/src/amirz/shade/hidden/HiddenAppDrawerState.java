package amirz.shade.hidden;

import android.content.Context;

import amirz.shade.util.AppReloader;

public class HiddenAppDrawerState {
    private static HiddenAppDrawerState sInstance;

    public static synchronized HiddenAppDrawerState getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new HiddenAppDrawerState(context);
        }
        return sInstance;
    }

    private final Context mContext;
    private boolean mRevealed;

    private HiddenAppDrawerState(Context context) {
        mContext = context;
    }

    public void toggleRevealed() {
        setRevealed(!isRevealed());
    }

    public void setRevealed(boolean revealed) {
        if (mRevealed != revealed) {
            mRevealed = revealed;
            AppReloader reloader = AppReloader.get(mContext);
            reloader.reload(reloader.hiddenApps());
        }
    }

    public boolean isRevealed() {
        return mRevealed;
    }
}
