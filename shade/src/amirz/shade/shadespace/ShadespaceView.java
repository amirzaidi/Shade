package amirz.shade.shadespace;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
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
import java.util.List;

public class ShadespaceView extends LinearLayout
        implements SwipeDetector.Listener, NotificationListener.NotificationsChangedListener {
    private final static float NOTIFICATION_OPEN_VELOCITY = 2.25f;
    private final static float NOTIFICATION_CLOSE_VELOCITY = -0.35f;

    private final BroadcastReceiver mTimeChangeReceiver;

    private boolean mNotificationsOpen;
    private DoubleShadowTextView mTopView;
    private DoubleShadowTextView mBottomView;

    private final List<NotificationKeyData> mNotifications = new ArrayList<>();
    private final List<StatusBarNotification> mSbn = new ArrayList<>();
    private final NotificationRanker mRanker;
    private final MediaListener mMedia;
    private final MultiClickListener mTaps;

    @SuppressLint({"ClickableViewAccessibility"})
    public ShadespaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTimeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reload();
            }
        };

        mRanker = new NotificationRanker(mSbn);
        mMedia = new MediaListener(context, mSbn, this::reload);

        mTaps = new MultiClickListener(300);
        mTaps.setListeners(mMedia::toggle, mMedia::next, mMedia::previous);

        SwipeDetector swipe = new SwipeDetector(context, this, SwipeDetector.VERTICAL);
        swipe.setDetectableScrollConditions(SwipeDetector.DIRECTION_BOTH, false);
        setOnTouchListener((v, event) -> swipe.onTouchEvent(event) && swipe.isDraggingState());
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTopView = findViewById(R.id.shadespace_text);
        mBottomView = findViewById(R.id.shadespace_subtext);

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
            if (velocity >= NOTIFICATION_OPEN_VELOCITY && !mNotificationsOpen) {
                cls.getMethod("expandNotificationsPanel").invoke(srv);
                mNotificationsOpen = true;
            } else if (velocity <= NOTIFICATION_CLOSE_VELOCITY && mNotificationsOpen) {
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

    private boolean mRunning;

    public void onResume() {
        mRunning = true;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        getContext().registerReceiver(mTimeChangeReceiver, intentFilter);
        mMedia.onResume(); // Triggers reload
    }

    public void onPause() {
        mRunning = false;
        getContext().unregisterReceiver(mTimeChangeReceiver);
        mMedia.onPause();
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
        // Do not update the content when we are paused.
        // This prevents the text from updating immediately when interacting with it.
        if (!mRunning) {
            return;
        }

        if (mMedia.isTracking()) {
            mTopView.setText(mMedia.getTitle());
            CharSequence app = getApp(mMedia.getPackage());
            if (TextUtils.isEmpty(mMedia.getArtist())) {
                mBottomView.setText(app);
            } else if (TextUtils.isEmpty(mMedia.getAlbum())
                    || mMedia.getTitle().equals(mMedia.getAlbum())) {
                mBottomView.setText(mMedia.getArtist());
            } else {
                mBottomView.setText(formatSubtext(mMedia.getArtist(), mMedia.getAlbum()));
            }
            setOnClickListener(mTaps);
        } else {
            // Default values
            mTopView.setText(DateUtils.formatDateTime(getContext(), System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE));
            setOnClickListener(this::openCalendar);

            NotificationRanker.RankedNotification ranked = mRanker.getBestNotification();
            if (ranked == null) {
                // Bottom view always gets overwritten by one of the other cases.
                mBottomView.setText(R.string.shadespace_subtext_default);
            } else {
                NotificationInfo notification = new NotificationInfo(getContext(), ranked.sbn);
                String text = notification.text == null
                        ? ""
                        : notification.text.toString().trim().split("\n")[0];
                if (ranked.important) {
                    CharSequence app = getApp(notification.packageUserKey.mPackageName);
                    if (TextUtils.isEmpty(text)) {
                        mTopView.setText(notification.title);
                        mBottomView.setText(app);
                    } else {
                        mTopView.setText(text);
                        mBottomView.setText(formatSubtext(app, notification.title));
                    }
                    setOnClickListener(notification);
                } else if (TextUtils.isEmpty(text)) {
                    mBottomView.setText(notification.title);
                } else {
                    mBottomView.setText(formatSubtext(notification.title, text));
                }
            }
        }
    }

    private String formatSubtext(CharSequence one, CharSequence two) {
        return getContext().getString(R.string.shadespace_subtext_double, one, two);
    }

    private CharSequence getApp(String name) {
        PackageManager pm = getContext().getPackageManager();
        try {
            return pm.getApplicationLabel(
                    pm.getApplicationInfo(name, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return name;
    }

    private void openCalendar(View v) {
        Uri.Builder timeUri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time");
        ContentUris.appendId(timeUri, System.currentTimeMillis());
        Intent addFlags = new Intent(Intent.ACTION_VIEW)
                .setData(timeUri.build())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        Launcher.getLauncher(getContext()).startActivitySafely(v, addFlags, null);
    }
}
