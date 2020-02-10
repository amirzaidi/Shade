package amirz.gesture;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
import static android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;

public class BarView extends LinearLayout implements View.OnApplyWindowInsetsListener {
    private final AccessibilityService mService;
    private int mGestureBarSize;
    private int mOffset;

    public BarView(AccessibilityService service) {
        super(service);
        mService = service;
        setBackgroundColor(0x00A0A0A0);

        Resources res = getContext().getResources();
        mGestureBarSize = res.getDimensionPixelSize(R.dimen.gesture_button_height);

        if (Utilities.ATLEAST_Q) {
            setOnApplyWindowInsetsListener(this);
        }
    }

    public void addToWm() {
        getContext().getSystemService(WindowManager.class).addView(this, getParams());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        }
        return super.onTouchEvent(ev);
    }

    @SuppressLint("NewApi")
    @Override
    public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        int bottomInset = insets.getMandatorySystemGestureInsets().bottom;
        if (bottomInset != 0) {
            mGestureBarSize = bottomInset;
            mOffset = bottomInset;
        }
        getContext().getSystemService(WindowManager.class).updateViewLayout(this, getParams());
        return insets.consumeSystemWindowInsets();
    }

    private WindowManager.LayoutParams getParams() {
        Resources res = getContext().getResources();
        int width = res.getDimensionPixelSize(R.dimen.gesture_button_width);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.width = width;
        params.height = mGestureBarSize;
        params.x = 0;
        params.y = -mOffset;
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.type = TYPE_ACCESSIBILITY_OVERLAY;
        params.flags = FLAG_LAYOUT_IN_SCREEN | FLAG_LAYOUT_NO_LIMITS | FLAG_NOT_FOCUSABLE;
        params.format = TRANSLUCENT;
        params.softInputMode = SOFT_INPUT_STATE_UNSPECIFIED;
        return params;
    }
}
