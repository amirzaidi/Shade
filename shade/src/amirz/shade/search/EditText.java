package amirz.shade.search;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.util.UiThreadHelper;

public class EditText extends ExtendedEditText {
    private CharSequence mHint;

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

    public void setSpannedHint(CharSequence hint) {
        mHint = hint;
        setHint(hint);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus) {
            setHint(null);
        } else {
            setHint(mHint);
        }
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

    @Override
    public void hideKeyboard() {
        UiThreadHelper.hideKeyboardSync(getContext(), getWindowToken());
    }
}
