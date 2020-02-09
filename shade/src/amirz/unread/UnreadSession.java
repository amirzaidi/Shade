package amirz.unread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.notification.NotificationListenerProxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import amirz.unread.calendar.DateBroadcastReceiver;
import amirz.unread.media.MediaListener;
import amirz.unread.notifications.NotificationList;
import amirz.unread.notifications.NotificationRanker;
import amirz.unread.notifications.ParsedNotification;

public class UnreadSession {
    private static final int NOTIF_UPDATE_DELAY = 750;
    private static final Intent BATTERY_INTENT = new Intent(Intent.ACTION_POWER_USAGE_SUMMARY);

    @SuppressLint("StaticFieldLeak")
    private static UnreadSession sInstance;

    public static synchronized UnreadSession getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UnreadSession(context.getApplicationContext());
        }
        return sInstance;
    }

    private final Context mContext;
    private final Handler mHandler = new Handler();

    private final Set<OnUpdateListener> mUpdateListeners = new HashSet<>();
    private final List<StatusBarNotification> mSbn = new ArrayList<>();
    private final NotificationRanker mRanker = new NotificationRanker(mSbn);
    private final NotificationList mNotifications = new NotificationList(this::onNotificationsChanged);

    private final MediaListener mMedia;
    private final DateBroadcastReceiver mDateReceiver;
    private final BatteryBroadcastReceiver mBatteryReceiver;

    private View.OnClickListener mOnClick;

    // Delay updates to keep the notification showing after pressing it.
    private long mLastClick;

    public interface OnUpdateListener {
        void onUpdateAvailable();
    }

    private UnreadSession(Context context) {
        mContext = context;

        mMedia = new MediaListener(context, this::reload);
        mDateReceiver = new DateBroadcastReceiver(context, this::reload);
        mBatteryReceiver = new BatteryBroadcastReceiver(context, this::reload);
    }

    public void onCreate() {
        NotificationListenerProxy.INSTANCE.add(mNotifications);
        mMedia.onResume();
        mDateReceiver.onResume();
        mBatteryReceiver.onResume();
    }

    public void onDestroy() {
        NotificationListenerProxy.INSTANCE.remove(mNotifications);
        mMedia.onPause();
        mDateReceiver.onPause();
        mBatteryReceiver.onPause();
    }

    public void addUpdateListener(OnUpdateListener listener) {
        mUpdateListeners.add(listener);
        listener.onUpdateAvailable();
    }

    public void removeUpdateListener(OnUpdateListener listener) {
        mUpdateListeners.remove(listener);
    }

    public void onClick(View v) {
        if (mOnClick != null) {
            mOnClick.onClick(v);
        }
    }

    public List<String> getText() {
        // Reset onClick, might not be necessary anymore.
        mOnClick = null;
        List<String> textList = new ArrayList<>();

        // 1. Playing media.
        if (mMedia.isTracking()) {
            textList.add(mMedia.getTitle().toString());
            CharSequence artist = mMedia.getArtist();
            if (TextUtils.isEmpty(artist)) {
                textList.add(getApp(mMedia.getPackage()).toString());
            } else {
                textList.add(artist.toString());
                CharSequence album = mMedia.getAlbum();
                if (!TextUtils.isEmpty(album) && !textList.contains(album.toString())) {
                    textList.add(album.toString());
                }
            }
            mOnClick = v -> mMedia.onClick();
            return textList;
        }

        NotificationRanker.RankedNotification ranked = mRanker.getBestNotification();

        // 2. Important notifications.
        if (ranked != null && ranked.important) {
            addImportantNotification(textList, new ParsedNotification(mContext, ranked.sbn));
            return textList;
        }

        // 3. Battery charging text (less than 100%) with date below.
        if (mBatteryReceiver.isCharging()) {
            int lvl = mBatteryReceiver.getLevel();
            if (lvl < 100) {
                textList.add(mContext.getString(
                        R.string.shadespace_text_charging, mBatteryReceiver.getLevel()));

                textList.add(DateUtils.formatDateTime(mContext, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE));

                mOnClick = v -> Launcher.getLauncher(v.getContext())
                        .startActivitySafely(v, BATTERY_INTENT, null, null);
            }
        }

        return textList;
    }

    private String stripDot(String input) {
        return input.length() > 1 && input.endsWith(".") && !input.endsWith("..")
                ? input.substring(0, input.length() - 1)
                : input;
    }

    private void addImportantNotification(List<String> textList, ParsedNotification parsed) {
        // Body on top if it is not empty.
        if (!TextUtils.isEmpty(parsed.body)) {
            textList.add(stripDot(parsed.body));
        }
        for (int i = parsed.splitTitle.length - 1; i >= 0; i--) {
            textList.add(stripDot(parsed.splitTitle[i]));
        }

        String app = getApp(parsed.pkg).toString();
        if (!textList.contains(app)) {
            textList.add(app);
        }

        mOnClick = v -> {
            if (parsed.pi != null) {
                mLastClick = System.currentTimeMillis();
                try {
                    Launcher launcher = Launcher.getLauncher(v.getContext());
                    Bundle b = launcher.getAppTransitionManager()
                            .getActivityLaunchOptions(launcher, v).toBundle();
                    parsed.pi.send(null, 0, null, null, null, null, b);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void reload() {
        long delayTime = Math.max(0, NOTIF_UPDATE_DELAY + mLastClick - System.currentTimeMillis());
        mHandler.postDelayed(() -> {
            for (OnUpdateListener listener : mUpdateListeners) {
                listener.onUpdateAvailable();
            }
        }, delayTime);
    }

    private CharSequence getApp(String name) {
        PackageManager pm = mContext.getPackageManager();
        try {
            return pm.getApplicationLabel(
                    pm.getApplicationInfo(name, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return name;
    }

    private void onNotificationsChanged() {
        mSbn.clear();
        if (mNotifications.hasNotifications()) {
            NotificationListener listener = NotificationListener.getInstanceIfConnected();
            if (listener != null) {
                mSbn.addAll(listener.getNotificationsForKeys(mNotifications.getKeys()));
            }
        }
        mMedia.onActiveSessionsChanged(null);
        reload();
    }
}
