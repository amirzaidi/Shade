package amirz.shade.shadespace;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.StatusBarNotification;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.android.launcher3.LauncherNotifications;
import com.android.launcher3.R;
import com.android.launcher3.notification.NotificationKeyData;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.touch.SwipeDetector;
import com.android.launcher3.util.PackageUserKey;

import java.util.ArrayList;
import java.util.List;

public class ShadespaceView extends LinearLayout
        implements SwipeDetector.Listener, NotificationListener.NotificationsChangedListener, ShadespaceController.DoubleLineView {
    private final static float NOTIFICATION_OPEN_VELOCITY = 2.25f;
    private final static float NOTIFICATION_CLOSE_VELOCITY = -0.35f;

    private final BroadcastReceiver mTimeChangeReceiver;

    private boolean mNotificationsOpen;
    private DoubleShadowTextView mTopView;
    private DoubleShadowTextView mBottomView;

    private final List<NotificationKeyData> mNotifications = new ArrayList<>();
    private final List<StatusBarNotification> mSbn = new ArrayList<>();
    private final MediaListener mMedia;
    private final ShadespaceController mController;

    @SuppressLint({"ClickableViewAccessibility"})
    public ShadespaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTimeChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                reload();
            }
        };

        mMedia = new MediaListener(context, mSbn, this::reload);

        SwipeDetector swipe = new SwipeDetector(context, this, SwipeDetector.VERTICAL);
        swipe.setDetectableScrollConditions(SwipeDetector.DIRECTION_BOTH, false);
        setOnTouchListener((v, event) -> swipe.onTouchEvent(event) && swipe.isDraggingState());

        mController = new ShadespaceController(this, mMedia, new NotificationRanker(mSbn));
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
        if (!mRunning) {
            mRunning = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_TIME_TICK);
            intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
            intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            getContext().registerReceiver(mTimeChangeReceiver, intentFilter);
            mMedia.onResume(); // Triggers reload
        }
    }

    public void onPause() {
        if (mRunning) {
            mRunning = false;
            getContext().unregisterReceiver(mTimeChangeReceiver);
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
        // Do not update the content when we are paused.
        // This prevents the text from updating immediately when interacting with it.
        if (mRunning) {
            mController.reload();
        }
    }

    @Override
    public void setTopText(CharSequence s) {
        mTopView.setText(s);
    }

    public void resetBottomText() {
        mBottomView.setText(R.string.shadespace_subtext_default);
    }

    @Override
    public void setBottomText(CharSequence s) {
        mBottomView.setText(s);
    }

    @Override
    public void setBottomTextSplit(CharSequence s1, CharSequence s2) {
        mBottomView.setText(getContext().getString(R.string.shadespace_subtext_double, s1, s2));
    }
}
