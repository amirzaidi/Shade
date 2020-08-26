package amirz.shade.views;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.LinearLayout;

import androidx.appcompat.widget.AppCompatTextView;

public class AutoShrinkTextView extends DoubleShadowTextView {
    private static final float MIN_SHRINK = 0.8f;

    private final TextPaint mPaintCopy = new TextPaint();
    private float mTextSize = Float.NaN;

    public AutoShrinkTextView(Context context) {
        this(context, null);
    }

    public AutoShrinkTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoShrinkTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if (getWidth() > 0) {
            resizeText();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (getWidth() > 0) {
            resizeText();
        }
    }

    private void resizeText() {
        float textSize = getTextSize();
        if (Float.isNaN(mTextSize)) {
            mTextSize = textSize;
            mPaintCopy.set(getPaint());
        }

        int w = ((LinearLayout) getParent()).getWidth() - getTotalPaddingLeft() - getTotalPaddingRight();
        float ratio = Math.min(1f, w * 0.96f / mPaintCopy.measureText(getText().toString()));
        float newTextSize = mTextSize * Math.max(MIN_SHRINK, ratio);

        if (newTextSize != textSize) {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, newTextSize);
        }
    }
}
