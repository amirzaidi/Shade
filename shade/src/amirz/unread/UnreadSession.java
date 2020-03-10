package amirz.unread;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherModel;
import com.android.launcher3.R;
import com.android.launcher3.notification.NotificationListenerProxy;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import amirz.unread.calendar.DateBroadcastReceiver;
import amirz.unread.media.MediaListener;
import amirz.unread.notifications.NotificationList;
import amirz.unread.notifications.NotificationRanker;
import amirz.unread.notifications.ParsedNotification;
import amirz.unread.notifications.PendingIntentSender;

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

    private final Context mAppContext;

    private final Set<OnUpdateListener> mUpdateListeners = new HashSet<>();
    private final PendingIntentSender mSender = new PendingIntentSender();

    private final Handler mWorkerHandler = new Handler(LauncherModel.getWorkerLooper());
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    private final Runnable mLoadText = () -> {
        // Load the new mEvent on the current thread.
        loadEvent();

        // Then update the UI on the UI thread.
        long timeSinceClick = System.currentTimeMillis() - mSender.getLastClick();
        long delay = Math.max(0, NOTIF_UPDATE_DELAY - timeSinceClick);
        mUiHandler.postDelayed(() -> {
            for (OnUpdateListener listener : mUpdateListeners) {
                listener.onUpdateAvailable();
            }
        }, delay);
    };

    private final Runnable mReload = () -> {
        // Collect all callbacks so we do not do excessive work but are always up to date.
        mWorkerHandler.removeCallbacks(mLoadText);
        mWorkerHandler.post(mLoadText);
    };

    private final NotificationList mNotifications = new NotificationList(mWorkerHandler, mReload);
    private final NotificationRanker mRanker = new NotificationRanker(mNotifications);

    private final MediaListener mMedia;
    private final DateBroadcastReceiver mDateReceiver;
    private final BatteryBroadcastReceiver mBatteryReceiver;

    private UnreadEvent mEvent = new UnreadEvent();

    public interface OnUpdateListener {
        void onUpdateAvailable();
    }

    private UnreadSession(Context appContext) {
        mAppContext = appContext;

        mMedia = new MediaListener(appContext, mWorkerHandler, mReload, mNotifications, mSender);
        mDateReceiver = new DateBroadcastReceiver(mReload);
        mBatteryReceiver = new BatteryBroadcastReceiver(appContext, mReload);

        NotificationListenerProxy.INSTANCE.add(mNotifications);
    }

    public void onCreate() {
        mWorkerHandler.post(mMedia::onCreate);
    }

    public void onResume(Context context) {
        mDateReceiver.onResume(context);
        mBatteryReceiver.onResume(context);

        // Always reload on resume.
        mReload.run();
    }

    public void onPause(Context context) {
        mDateReceiver.onPause(context);
        mBatteryReceiver.onPause(context);
    }

    public void onDestroy() {
        mWorkerHandler.post(mMedia::onDestroy);
    }

    public void addUpdateListener(OnUpdateListener listener) {
        mUpdateListeners.add(listener);
        listener.onUpdateAvailable();
    }

    public void removeUpdateListener(OnUpdateListener listener) {
        mUpdateListeners.remove(listener);
    }

    public UnreadEvent getEvent() {
        return mEvent;
    }

    private void loadEvent() {
        UnreadEvent event = new UnreadEvent();
        List<String> textList = event.getText();
        textList.clear();

        // 1. Important notifications.
        NotificationRanker.RankedNotification ranked = mRanker.getBestNotification();
        if (ranked != null && ranked.important) {
            extractNotification(ranked.sbn, event);
        }
        // 2. Playing media.
        else if (mMedia.isPausedOrPlaying()) {
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
            event.setOnClickListener(mMedia);
        }
        // 3. Important notifications.
        else if (ranked != null) {
            extractNotification(ranked.sbn, event);
        }
        // 4. Battery charging mText (less than 100%) with date below.
        else if (mBatteryReceiver.isCharging()) {
            int lvl = mBatteryReceiver.getLevel();
            if (lvl < 100) {
                textList.add(mAppContext.getString(
                        R.string.shadespace_text_charging, mBatteryReceiver.getLevel()));
                textList.add(DateUtils.formatDateTime(mAppContext, System.currentTimeMillis(),
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE));

                event.setOnClickListener(v -> Launcher.getLauncher(v.getContext())
                        .startActivitySafely(v, BATTERY_INTENT, null, null));
            }
        }

        // Commit to new event.
        mEvent = event;
    }

    private String stripDot(String input) {
        return input.length() > 1 && input.endsWith(".") && !input.endsWith("..")
                ? input.substring(0, input.length() - 1)
                : input;
    }

    private void extractNotification(StatusBarNotification sbn, UnreadEvent event) {
        List<String> textList = event.getText();
        ParsedNotification parsed = new ParsedNotification(mAppContext, sbn);

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

        event.setOnClickListener(mSender.onClickNotification(parsed.pi));
    }

    private CharSequence getApp(String name) {
        PackageManager pm = mAppContext.getPackageManager();
        try {
            return pm.getApplicationLabel(
                    pm.getApplicationInfo(name, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return name;
    }
}
