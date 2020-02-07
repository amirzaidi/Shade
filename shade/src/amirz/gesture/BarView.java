package amirz.gesture;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.android.launcher3.R;

import static android.graphics.PixelFormat.TRANSLUCENT;
import static android.view.WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
import static android.view.WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_UNSPECIFIED;
import static android.view.WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;

public class BarView extends LinearLayout {
    private static final int FLAG_SLIPPERY = 0x20000000;

    private final AccessibilityService mService;

    public BarView(AccessibilityService service) {
        super(service);
        mService = service;
        setBackgroundColor(0x00FFFFFF);
    }

    public void addToWm() {
        Resources res = mService.getResources();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.x = 0;
        params.y = 0;
        params.width = res.getDimensionPixelSize(R.dimen.gesture_button_width);
        params.height = res.getDimensionPixelSize(R.dimen.gesture_button_height);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        params.type = TYPE_ACCESSIBILITY_OVERLAY;
        params.flags = FLAG_LAYOUT_IN_SCREEN | FLAG_NOT_FOCUSABLE | FLAG_SLIPPERY;
        params.format = TRANSLUCENT;
        params.softInputMode = SOFT_INPUT_STATE_UNSPECIFIED;

        getContext().getSystemService(WindowManager.class).addView(this, params);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mService.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
        }
        return super.onTouchEvent(ev);
    }
}
