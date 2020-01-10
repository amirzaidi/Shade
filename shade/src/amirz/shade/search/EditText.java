package amirz.shade.search;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.android.launcher3.ExtendedEditText;

public class EditText extends ExtendedEditText {
    public EditText(Context context) {
        // ctor chaining breaks the touch handling
        super(context);
    }

    public EditText(Context context, AttributeSet attrs) {
        // ctor chaining breaks the touch handling
        super(context, attrs);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void reset() {
        if (!TextUtils.isEmpty(getText())) {
            setText("");
        }
        if (isFocused()) {
            View nextFocus = focusSearch(View.FOCUS_DOWN);
            if (nextFocus != null) {
                nextFocus.requestFocus();
            }
        }
    }
}
