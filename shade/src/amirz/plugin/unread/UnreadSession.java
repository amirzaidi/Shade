package amirz.plugin.unread;

import android.app.PendingIntent;
import android.content.Context;
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
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.plugin.unread.IUnreadPlugin;
import com.android.launcher3.plugin.unread.IUnreadPluginCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnreadSession extends IUnreadPlugin.Stub {
    private static final int MULTI_CLICK_DELAY = 300;

    private final Context mContext;
    private final Set<IUnreadPluginCallback> mCallbacks = new HashSet<>();

    private final List<StatusBarNotification> mSbn = new ArrayList<>();
    private final NotificationRanker mRanker = new NotificationRanker(mSbn);
    private final NotificationList mNotifications = new NotificationList(this::onNotificationsChanged);

    private final MediaListener mMedia;
    private final MultiClickListener mTaps;

    private final DateBroadcastReceiver mDateReceiver;
    private final CalendarReceiver mCalendarReceiver;
    private final BatteryBroadcastReceiver mBatteryReceiver;
    private final IconBadgingObserver mBadgingObserver;

    private OnClickListener mOnClick;

    private interface OnClickListener {
        void onClick(Bundle launchOptions);
    }

    UnreadSession(Context context) {
        mContext = context;

        mMedia = new MediaListener(context, mSbn, this::reload);
        mTaps = new MultiClickListener(MULTI_CLICK_DELAY);
        mTaps.setListeners(mMedia::toggle, mMedia::next, mMedia::previous);
        mDateReceiver = new DateBroadcastReceiver(context, this::reload);
        mCalendarReceiver = new CalendarReceiver(context, this::reload);
        mBatteryReceiver = new BatteryBroadcastReceiver(context, this::reload);
        mBadgingObserver = new IconBadgingObserver(context, this::reload);
        mBadgingObserver.register();

        LauncherNotifications.getInstance().addUnfilteredListener(mNotifications);
    }

    @Override
    public List<String> getText() {
        List<String> textList = new ArrayList<>();

        // 0. Permission
        if (!mBadgingObserver.isBadgingEnabled()) {
            textList.add(mContext.getString(R.string.title_missing_notification_access));
            textList.add(mContext.getString(R.string.title_change_settings));
            mOnClick = mBadgingObserver::onClick;
            return textList;
        }

        // 1. Media
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
            mOnClick = launchOptions -> mTaps.onClick();
            return textList;
        }

        NotificationRanker.RankedNotification ranked = mRanker.getBestNotification();

        String app = null;
        String[] splitTitle = null;
        String body = null;

        // 2. High priority notification
        if (ranked != null) {
            NotificationInfo notif = new NotificationInfo(mContext, ranked.sbn);
            app = getApp(notif.packageUserKey.mPackageName).toString();
            String title = notif.title == null
                    ? ""
                    : notif.title.toString();
            splitTitle = splitTitle(title);
            body = notif.text == null
                    ? ""
                    : notif.text.toString().trim().split("\n")[0]; // First line

            if (ranked.important) {
                // Body on top if it is not empty.
                if (!TextUtils.isEmpty(body)) {
                    textList.add(body);
                }
                for (int i = splitTitle.length - 1; i >= 0; i--) {
                    textList.add(splitTitle[i]);
                }

                PendingIntent pi = notif.intent;
                mOnClick = launchOptions -> {
                    if (pi != null) {
                        try {
                            if (Utilities.ATLEAST_MARSHMALLOW) {
                                pi.send(null, 0, null, null, null, null, launchOptions);
                            } else {
                                pi.send();
                            }
                        } catch (PendingIntent.CanceledException e) {
                            e.printStackTrace();
                        }
                    }
                };
                if (!textList.contains(app)) {
                    textList.add(app);
                }
                return textList;
            }
        }

        // 3. Calendar event
        mOnClick = mDateReceiver::openCalendar;
        CalendarParser.Event event = CalendarParser.getEvent(mContext);
        if (event != null) {
            textList.add(event.name);
            int flags = DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_WEEKDAY;
            textList.add(DateUtils.formatDateTime(mContext, event.start.getTimeInMillis(), flags));
            if (event.start.get(Calendar.DAY_OF_WEEK) == event.end.get(Calendar.DAY_OF_WEEK)) {
                flags &= ~DateUtils.FORMAT_SHOW_WEEKDAY;
            }
            textList.add(DateUtils.formatDateTime(mContext, event.end.getTimeInMillis(), flags));
            return textList;
        }

        // 4. Date (Reuse open calendar onClick)
        textList.add(DateUtils.formatDateTime(mContext, System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE));

        // 4a. With normal notification
        if (ranked != null) {
            for (int i = splitTitle.length - 1; i >= 0; i--) {
                textList.add(splitTitle[i]);
            }
            if (!TextUtils.isEmpty(body)) {
                textList.add(body);
            }
            if (!textList.contains(app)) {
                textList.add(app);
            }
        // 4b. With battery charging text
        } else if (mBatteryReceiver.isCharging()) {
            textList.add(mContext.getString(R.string.shadespace_subtext_charging,
                    mBatteryReceiver.getLevel()));
        }

        return textList;
    }

    @Override
    public void clickView(int index, Bundle launchOptions) {
        if (mOnClick != null) {
            mOnClick.onClick(launchOptions);
        }
    }

    private String[] splitTitle(String title) {
        final String[] delimiters = { ": ", " - ", " • " };
        for (String del : delimiters) {
            if (title.contains(del)) {
                return title.split(del, 2);
            }
        }
        return new String[] { title };
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
            mCalendarReceiver.onResume();
            mBatteryReceiver.onResume();
            mMedia.onResume();
        }
        mCallbacks.add(cb);
    }

    @Override
    public void removeOnChangeListener(IUnreadPluginCallback cb) {
        mCallbacks.remove(cb);
        if (mCallbacks.isEmpty()) {
            mDateReceiver.onPause();
            mCalendarReceiver.onPause();
            mBatteryReceiver.onPause();
            mMedia.onPause();
        }
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
