package com.android.launcher3.uioverrides;

import android.annotation.SuppressLint;
import android.view.MotionEvent;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.Launcher;
import com.android.launcher3.touch.SwipeDetector;
import com.android.launcher3.util.TouchController;

import java.lang.reflect.Method;

import static com.android.launcher3.LauncherState.NORMAL;

public class NotificationController implements TouchController, SwipeDetector.Listener {
    // Swipe speed needed to open or close notifications.
    private final static float NOTIFICATION_OPEN_VELOCITY = 2.25f;
    private final static float NOTIFICATION_CLOSE_VELOCITY = -0.35f;

    private final Launcher mLauncher;
    private final SwipeDetector mGesture;

    private Method mExpand;
    private Method mCollapse;
    private Object mSbm;
    private boolean mOpened;

    @SuppressLint({"PrivateApi", "WrongConstant"})
    NotificationController(Launcher launcher) {
        mLauncher = launcher;
        mGesture = new SwipeDetector(launcher, this, SwipeDetector.VERTICAL);
        mGesture.setDetectableScrollConditions(SwipeDetector.DIRECTION_NEGATIVE, false);

        try {
            Class<?> cls = Class.forName("android.app.StatusBarManager");
            mExpand = cls.getMethod("expandNotificationsPanel");
            mCollapse = cls.getMethod("collapsePanels");
            mSbm = mLauncher.getSystemService("statusbar");
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean onControllerTouchEvent(MotionEvent ev) {
        return mGesture.onTouchEvent(ev);
    }

    @Override
    public boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        if (AbstractFloatingView.getTopOpenView(mLauncher) != null) {
            return false;
        }
        if (!mLauncher.isInState(NORMAL)) {
            return false;
        }
        mGesture.onTouchEvent(ev);
        return mOpened;
    }

    @Override
    public void onDragStart(boolean start) {
    }

    @Override
    public boolean onDrag(float velocity) {
        if (mSbm != null) {
            try {
                if (velocity >= NOTIFICATION_OPEN_VELOCITY) {
                    mGesture.setDetectableScrollConditions(
                            SwipeDetector.DIRECTION_POSITIVE, false);
                    mExpand.invoke(mSbm);
                    mOpened = true;
                } else if (velocity <= NOTIFICATION_CLOSE_VELOCITY) {
                    mGesture.setDetectableScrollConditions(
                            SwipeDetector.DIRECTION_NEGATIVE, false);
                    mCollapse.invoke(mSbm);
                }
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    @Override
    public void onDragEnd(float velocity, boolean fling) {
        mGesture.setDetectableScrollConditions(SwipeDetector.DIRECTION_NEGATIVE, false);
        mOpened = false;
    }
}
