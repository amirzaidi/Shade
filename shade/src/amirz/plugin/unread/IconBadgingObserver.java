package amirz.plugin.unread;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.util.SettingsObserver;

import static com.android.launcher3.SettingsActivity.NOTIFICATION_BADGING;

class IconBadgingObserver extends SettingsObserver.Secure {
    private static final String NOTIFICATION_ENABLED_LISTENERS = "enabled_notification_listeners";
    private static final String EXTRA_FRAGMENT_ARG_KEY = ":settings:fragment_args_key";
    private static final String EXTRA_SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args";

    private final Context mContext;
    private final ContentResolver mResolver;
    private final Runnable mOnReceive;
    private final ComponentName mCn;
    private boolean mBadgingEnabled = true;

    IconBadgingObserver(Context context, Runnable onReceive) {
        super(context.getContentResolver());
        mContext = context;
        mResolver = context.getContentResolver();
        mOnReceive = onReceive;
        mCn = new ComponentName(mContext, NotificationListener.class);
    }

    void register() {
        register(NOTIFICATION_BADGING, NOTIFICATION_ENABLED_LISTENERS);
    }

    boolean isBadgingEnabled() {
        return mBadgingEnabled;
    }

    void onClick(Bundle activityOptions) {
        Bundle showFragmentArgs = new Bundle();
        showFragmentArgs.putString(EXTRA_FRAGMENT_ARG_KEY, mCn.flattenToString());
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(EXTRA_FRAGMENT_ARG_KEY, mCn.flattenToString())
                .putExtra(EXTRA_SHOW_FRAGMENT_ARGS, showFragmentArgs);
        mContext.startActivity(intent, activityOptions);
    }

    @Override
    public void onSettingChanged(boolean enabled) {
        if (enabled) {
            // Check if the listener is enabled or not.
            String enabledListeners =
                    Settings.Secure.getString(mResolver, NOTIFICATION_ENABLED_LISTENERS);
            mBadgingEnabled = enabledListeners != null &&
                    (enabledListeners.contains(mCn.flattenToString()) ||
                            enabledListeners.contains(mCn.flattenToShortString()));
            mOnReceive.run();
        }
    }
}
