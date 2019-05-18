package amirz.plugin.unread;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.android.launcher3.LauncherNotifications;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.notification.NotificationInfo;
import com.android.launcher3.notification.NotificationKeyData;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.plugin.unread.IUnreadPlugin;
import com.android.launcher3.plugin.unread.IUnreadPluginCallback;
import com.android.launcher3.util.PackageUserKey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnreadSession extends IUnreadPlugin.Stub implements NotificationListener.NotificationsChangedListener {
    private static final int MULTI_CLICK_DELAY = 300;

    private final Context mContext;
    private final Set<IUnreadPluginCallback> mCallbacks = new HashSet<>();

    private final List<NotificationKeyData> mNotifications = new ArrayList<>();
    private final List<StatusBarNotification> mSbn = new ArrayList<>();
    private final NotificationRanker mRanker = new NotificationRanker(mSbn);

    private final MediaListener mMedia;
    private final MultiClickListener mTaps;
    private final DateBroadcastReceiver mDateReceiver;

    private OnClickListener mOnClick;

    private interface OnClickListener {
        void onClick(Bundle launchOptions);
    }

    UnreadSession(Context context) {
        mContext = context;

        mMedia = new MediaListener(context, mSbn, this::reload);
        mTaps = new MultiClickListener(MULTI_CLICK_DELAY);
        mTaps.setListeners(mMedia::toggle, mMedia::next, mMedia::previous);
        mDateReceiver = new DateBroadcastReceiver(context) {
            @Override
            public void onReceive(Context context, Intent intent) {
                reload();
            }
        };

        LauncherNotifications.getInstance().addListener(this);
    }

    @Override
    public List<String> getText() {
        List<String> textList = new ArrayList<>();
        if (mMedia.isTracking()) {
            textList.add(mMedia.getTitle().toString());
            if (TextUtils.isEmpty(mMedia.getArtist())) {
                textList.add(getApp(mMedia.getPackage()).toString());
            } else {
                textList.add(mMedia.getArtist().toString());
                if (!TextUtils.isEmpty(mMedia.getAlbum())
                        && !mMedia.getTitle().equals(mMedia.getAlbum())) {
                    textList.add(mMedia.getAlbum().toString());
                }
            }
            mOnClick = launchOptions -> mTaps.onClick();
        } else {
            textList.add(DateUtils.formatDateTime(mContext, System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE));
            mOnClick = mDateReceiver::openCalendar;

            NotificationRanker.RankedNotification ranked = mRanker.getBestNotification();
            if (ranked == null) {
                textList.add(mContext.getString(R.string.shadespace_subtext_default));
            } else {
                NotificationInfo notif = new NotificationInfo(mContext, ranked.sbn);
                String app = getApp(notif.packageUserKey.mPackageName).toString();
                String title = notif.title == null
                        ? ""
                        : notif.title.toString();
                String body = notif.text == null
                        ? ""
                        : notif.text.toString().trim().split("\n")[0];

                if (ranked.important) {
                    textList.clear();
                    if (!TextUtils.isEmpty(body)) {
                        textList.add(body);
                    }
                    textList.add(title);

                    PendingIntent pi = notif.intent;
                    mOnClick = launchOptions -> {
                        try {
                            if (Utilities.ATLEAST_MARSHMALLOW) {
                                pi.send(null, 0, null, null, null, null, launchOptions);
                            } else {
                                pi.send();
                            }
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    };
                } else {
                    textList.add(title);
                    if (!TextUtils.isEmpty(body)) {
                        textList.add(body);
                    }
                }

                if (shouldShowAppName(title, app)) {
                    textList.add(app);
                }
            }
        }
        return textList;
    }

    @Override
    public void clickView(int index, Bundle launchOptions) {
        if (mOnClick != null) {
            mOnClick.onClick(launchOptions);
        }
    }

    private boolean shouldShowAppName(String title, String app) {
        return !title.contains(": ") && !title.contains(" - ") && !title.equals(app);
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

    @Override
    public void addOnChangeListener(IUnreadPluginCallback cb) {
        if (mCallbacks.isEmpty()) {
            mDateReceiver.onResume();
            mMedia.onResume();
        }
        mCallbacks.add(cb);
    }

    @Override
    public void removeOnChangeListener(IUnreadPluginCallback cb) {
        mCallbacks.remove(cb);
        if (mCallbacks.isEmpty()) {
            mDateReceiver.onPause();
            mMedia.onPause();
        }
    }

    @Override
    public void onNotificationPosted(PackageUserKey postedPackageUserKey,
                                     NotificationKeyData notificationKey,
                                     boolean shouldBeFilteredOut) {
        if (!shouldBeFilteredOut) {
            mNotifications.remove(notificationKey);
            mNotifications.add(notificationKey);
            onNotificationsChanged();
        }
    }

    @Override
    public void onNotificationRemoved(PackageUserKey removedPackageUserKey,
                                      NotificationKeyData notificationKey) {
        if (mNotifications.remove(notificationKey)) {
            onNotificationsChanged();
        }
    }

    @Override
    public void onNotificationFullRefresh(List<StatusBarNotification> activeNotifications) {
        mNotifications.clear();
        for (int i = activeNotifications.size() - 1; i >= 0; i--) {
            mNotifications.add(NotificationKeyData.fromNotification(activeNotifications.get(i)));
        }
        onNotificationsChanged();
    }

    private void onNotificationsChanged() {
        mSbn.clear();
        if (!mNotifications.isEmpty()) {
            NotificationListener notificationListener = NotificationListener.getInstanceIfConnected();
            if (notificationListener != null) {
                mSbn.addAll(notificationListener.getNotificationsForKeys(mNotifications));
            }
        }
        mMedia.onActiveSessionsChanged(null);
        reload();
    }

    private void reload() {
        for (IUnreadPluginCallback callback : new HashSet<>(mCallbacks)) {
            try {
                callback.onChange();
            } catch (RemoteException e) {
                mCallbacks.remove(callback);
            }
        }
    }
}
