package amirz.shade.allapps;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class TabButton extends Button {
    public TabButton(Context context) {
        super(context);
    }

    public TabButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TabButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        setTransformationMethod(null);
    }
}
