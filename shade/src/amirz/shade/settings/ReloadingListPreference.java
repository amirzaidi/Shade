package amirz.shade.settings;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.ListPreference;

import com.android.launcher3.R;

import java.util.function.Function;

import amirz.shade.ShadeSettings;

import static com.android.launcher3.util.Executors.MAIN_EXECUTOR;
import static com.android.launcher3.util.Executors.THREAD_POOL_EXECUTOR;

@SuppressWarnings("unused")
public class ReloadingListPreference extends ListPreference
        implements ShadeSettings.OnResumePreferenceCallback {
    public interface OnReloadListener {
        Runnable listUpdater(ListPreference pref);
    }

    private OnReloadListener mOnReloadListener;

    public ReloadingListPreference(Context context) {
        super(context);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ReloadingListPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                   int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onClick() {
        // Run the entries updater on the main thread immediately.
        // Should be fast as the data was cached from the async load before.
        // If it wasn't, we need to block to ensure the data has been loaded.
        loadEntries(false);
        super.onClick();
    }

    public void setOnReloadListener(Function<Context, OnReloadListener> supplier) {
        mOnReloadListener = supplier.apply(getContext());
        loadEntries(true);
    }
    
    @Override
    public void onResume() {
        loadEntries(true);
    }

    private void loadEntries(boolean async) {
        if (mOnReloadListener != null) {
            if (async) {
                if (getEntryValues() == null) {
                    setSummary(R.string.loading);
                }
                THREAD_POOL_EXECUTOR.execute(() -> {
                        Runnable uiRunnable = mOnReloadListener.listUpdater(this);
                        MAIN_EXECUTOR.execute(() -> {
                            uiRunnable.run();
                            setSummary("%s");
                        });
                });
            } else {
                mOnReloadListener.listUpdater(this).run();
            }
        }
    }
}
