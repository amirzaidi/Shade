package com.android.launcher3.uioverrides.touchcontrollers;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.util.TouchController;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_MOVE;
import static android.view.MotionEvent.ACTION_UP;

public class StatusBarTouchController implements TouchController {
    /**
     * Window flag: Enable touches to slide out of a window into neighboring
     * windows in mid-gesture instead of being captured for the duration of
     * the gesture.
     *
     * This flag changes the behavior of touch focus for this window only.
     * Touches can slide out of the window but they cannot necessarily slide
     * back in (unless the other window with touch focus permits it).
     */
    private static final int FLAG_SLIPPERY = 0x20000000;

    private final Launcher mLauncher;

    private final float mTouchSlop;
    private final SparseArray<PointF> mDownEvents;

    /* If {@code false}, this controller should not handle the input {@link MotionEvent}.*/
    private boolean mCanIntercept;

    private Method mExpand;
    private Object mSbm;

    @SuppressWarnings("JavaReflectionMemberAccess")
    @SuppressLint({"PrivateApi", "WrongConstant"})
    public StatusBarTouchController(Launcher launcher) {
        mLauncher = launcher;

        // Guard against TAPs by increasing the touch slop.
        mTouchSlop = 2 * ViewConfiguration.get(launcher).getScaledTouchSlop();
        mDownEvents = new SparseArray<>();

        try {
            Class<?> cls = Class.forName("android.app.StatusBarManager");
            mExpand = cls.getMethod("expandNotificationsPanel");
            mSbm = mLauncher.getSystemService("statusbar");
        } catch (Exception ignored) {
        }
    }

    @Override
    public void dump(String prefix, PrintWriter writer) {
        writer.println(prefix + "mCanIntercept:" + mCanIntercept);
    }

    @Override
    public final boolean onControllerInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getActionMasked();
        int idx = ev.getActionIndex();
        int pid = ev.getPointerId(idx);
        if (action == ACTION_DOWN) {
            mCanIntercept = canInterceptTouch(ev);
            if (!mCanIntercept) {
                return false;
            }
            mDownEvents.put(pid, new PointF(ev.getX(), ev.getY()));
        } else if (ev.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
            // Check!! should only set it only when threshold is not entered.
            mDownEvents.put(pid, new PointF(ev.getX(idx), ev.getY(idx)));
        }
        if (!mCanIntercept) {
            return false;
        }
        if (action == ACTION_MOVE) {
            float dy = ev.getY(idx) - mDownEvents.get(pid).y;
            float dx = ev.getX(idx) - mDownEvents.get(pid).x;
            // Currently input dispatcher will not do touch transfer if there are more than
            // one touch pointer. Hence, even if slope passed, only set the slippery flag
            // when there is single touch event. (context: InputDispatcher.cpp line 1445)
            if (dy > mTouchSlop && dy > Math.abs(dx) && ev.getPointerCount() == 1) {
                try {
                    mExpand.invoke(mSbm);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
                setWindowSlippery(true);
                return true;
            }
            if (Math.abs(dx) > mTouchSlop) {
                mCanIntercept = false;
            }
        }
        return false;
    }

    @Override
    public final boolean onControllerTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (action == ACTION_UP || action == ACTION_CANCEL) {
            mLauncher.getUserEventDispatcher().logActionOnContainer(action == ACTION_UP
                            ? LauncherLogProto.Action.Touch.FLING
                            : LauncherLogProto.Action.Touch.SWIPE,
                    LauncherLogProto.Action.Direction.DOWN,
                    LauncherLogProto.ContainerType.WORKSPACE,
                    mLauncher.getWorkspace().getCurrentPage());
            setWindowSlippery(false);
            return true;
        }
        return true;
    }

    private void setWindowSlippery(boolean enable) {
        Window w = mLauncher.getWindow();
        WindowManager.LayoutParams wlp = w.getAttributes();
        if (enable) {
            wlp.flags |= FLAG_SLIPPERY;
        } else {
            wlp.flags &= ~FLAG_SLIPPERY;
        }
        w.setAttributes(wlp);
    }

    private boolean canInterceptTouch(MotionEvent ev) {
        if (!mLauncher.isInState(LauncherState.NORMAL) ||
                AbstractFloatingView.getTopOpenViewWithType(mLauncher,
                        AbstractFloatingView.TYPE_STATUS_BAR_SWIPE_DOWN_DISALLOW) != null) {
            return false;
        } else {
            // For NORMAL state, only listen if the event originated above the navbar height
            DeviceProfile dp = mLauncher.getDeviceProfile();
            if (ev.getY() > (mLauncher.getDragLayer().getHeight() - dp.getInsets().bottom)) {
                return false;
            }
        }
        return true;
    }
}
