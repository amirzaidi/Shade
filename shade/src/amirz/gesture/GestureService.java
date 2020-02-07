package amirz.gesture;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

public class GestureService extends AccessibilityService {
    private BarView mView;

    @Override
    public void onCreate() {
        super.onCreate();
        mView = new BarView(this);
    }

    @Override
    protected void onServiceConnected() {
        mView.addToWm();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    @Override
    public void onInterrupt() {
    }
}
