package amirz.gesture;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class GestureService extends AccessibilityService {
    private BarView mView;

    @Override
    protected void onServiceConnected() {
        if (mView == null) {
            mView = new BarView(this);
        }
        mView.addToWm();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
