package amirz.shade.shadespace;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.provider.CalendarContract;
import android.service.notification.StatusBarNotification;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherNotifications;
import com.android.launcher3.R;
import com.android.launcher3.notification.NotificationInfo;
import com.android.launcher3.notification.NotificationKeyData;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.touch.SwipeDetector;
import com.android.launcher3.util.PackageUserKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.app.Notification.PRIORITY_DEFAULT;

public class ShadespaceView extends LinearLayout
        implements SwipeDetector.Listener, NotificationListener.NotificationsChangedListener {
    private final static float NOTIFICATION_OPEN_VELOCITY = 2.25f;
    private final static float NOTIFICATION_CLOSE_VELOCITY = -0.35f;

    private final BroadcastReceiver mTimeChangeReceiver;

    private boolean mNotificationsOpen;
    private DoubleShadowTextView mTopView;
    private DoubleShadowTextView mBottomView;

    private List<NotificationKeyData> mNotifications = new ArrayList<>();

    @SuppressLint({"ClickableViewAccessibility"})
    public ShadespaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTimeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reload();
            }
        };

        SwipeDetector swipe = new SwipeDetector(context, this, SwipeDetector.VERTICAL);
        swipe.setDetectableScrollConditions(SwipeDetector.DIRECTION_BOTH, false);
        setOnTouchListener((v, event) -> swipe.onTouchEvent(event) && swipe.isDraggingState());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTopView = findViewById(R.id.shadespace_text);
        mBottomView = findViewById(R.id.shadespace_subtext);

        reload();
        LauncherNotifications.getInstance().addListener(this);
    }

    @Override
    public void onDragStart(boolean start) {
        mNotificationsOpen = false;
    }

    @SuppressLint({"PrivateApi", "WrongConstant"})
    @Override
    public boolean onDrag(float displacement, float velocity) {
        try {
            Class<?> cls = Class.forName("android.app.StatusBarManager");
            Object srv = getContext().getSystemService("statusbar");
            if (velocity > NOTIFICATION_OPEN_VELOCITY && !mNotificationsOpen) {
                cls.getMethod("expandNotificationsPanel").invoke(srv);
                mNotificationsOpen = true;
            } else if (velocity < NOTIFICATION_CLOSE_VELOCITY && mNotificationsOpen) {
                cls.getMethod("collapsePanels").invoke(srv);
                mNotificationsOpen = false;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    public void onDragEnd(float velocity, boolean fling) {
    }

    public void onResume() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mTimeChangeReceiver, intentFilter);
    }

    public void onPause() {
        getContext().unregisterReceiver(mTimeChangeReceiver);
    }

    @Override
    public void onNotificationPosted(PackageUserKey postedPackageUserKey,
                                     NotificationKeyData notificationKey,
                                     boolean shouldBeFilteredOut) {
        if (!shouldBeFilteredOut) {
            mNotifications.remove(notificationKey);
            mNotifications.add(notificationKey);
            reload();
        }
    }

    @Override
    public void onNotificationRemoved(PackageUserKey removedPackageUserKey,
                                      NotificationKeyData notificationKey) {
        if (mNotifications.remove(notificationKey)) {
            reload();
        }
    }

    @Override
    public void onNotificationFullRefresh(List<StatusBarNotification> activeNotifications) {
        mNotifications.clear();
        for (int i = activeNotifications.size() - 1; i >= 0; i--) {
            mNotifications.add(NotificationKeyData.fromNotification(activeNotifications.get(i)));
        }
        reload();
    }

    private void reload() {
        String dateString = DateUtils.formatDateTime(getContext(), System.currentTimeMillis(),
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE);
        mTopView.setText(dateString);

        List<StatusBarNotification> sbn = getNotifications();
        if (sbn.isEmpty()) {
            mBottomView.setText(R.string.shadespace_subtext_default);
            setOnClickListener(this::openCalendar);
        } else {
            int notifCount = nonPersistentNotificationCount(sbn);
            if (notifCount > 0) {
                mTopView.setText(getContext().getResources().getQuantityString(
                        R.plurals.shadespace_text_notif, notifCount, notifCount));
            }

            StatusBarNotification topNotif = mostImportantNotification(sbn);
            NotificationInfo notification = new NotificationInfo(getContext(), topNotif);
            mBottomView.setText(notification.text == null
                    ? notification.title
                    : getContext().getString(
                            R.string.shadespace_subtext_notif,
                            notification.title,
                            notification.text.toString().split("\n")[0]));

            setOnClickListener(notification);
        }
    }

    private void openCalendar(View v) {
        Uri.Builder timeUri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time");
        ContentUris.appendId(timeUri, System.currentTimeMillis());
        Intent addFlags = new Intent(Intent.ACTION_VIEW)
                .setData(timeUri.build())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        Launcher.getLauncher(getContext()).startActivitySafely(v, addFlags, null);
    }

    private List<StatusBarNotification> getNotifications() {
        if (!mNotifications.isEmpty()) {
            NotificationListener notificationListener = NotificationListener.getInstanceIfConnected();
            if (notificationListener != null) {
                return notificationListener.getNotificationsForKeys(mNotifications);
            }
        }
        return Collections.EMPTY_LIST;
    }

    private int nonPersistentNotificationCount(List<StatusBarNotification> sbn) {
        int count = 0;
        for (StatusBarNotification notif : sbn) {
            if (!notif.isOngoing() && defaultPriorityOrHigher(notif)) {
                count++;
            }
        }
        return count;
    }

    private StatusBarNotification mostImportantNotification(List<StatusBarNotification> sbn) {
        for (StatusBarNotification notif : sbn) {
            if (notif.isOngoing() && defaultPriorityOrHigher(notif)) {
                return notif;
            }
        }
        return sbn.get(sbn.size() - 1);
    }

    private boolean defaultPriorityOrHigher(StatusBarNotification notif) {
        return notif.getNotification().priority >= PRIORITY_DEFAULT;
    }
}
