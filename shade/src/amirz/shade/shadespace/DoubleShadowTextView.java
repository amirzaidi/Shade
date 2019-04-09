package amirz.shade.shadespace;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import com.android.launcher3.views.DoubleShadowBubbleTextView;

public class DoubleShadowTextView extends TextView {
    private static final float MIN_SHRINK = 0.85f;
    private final DoubleShadowBubbleTextView.ShadowInfo mShadowInfo;
    private float mTextSize = Float.NaN;

    public DoubleShadowTextView(Context context) {
        this(context, null);
    }

    public DoubleShadowTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleShadowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mShadowInfo = new DoubleShadowBubbleTextView.ShadowInfo(context, attrs, defStyleAttr);
        setShadowLayer(Math.max(mShadowInfo.keyShadowBlur + mShadowInfo.keyShadowOffset,
                mShadowInfo.ambientShadowBlur),
                0f, 0f, mShadowInfo.keyShadowColor);
    }

    protected void onDraw(Canvas canvas) {
        if (mShadowInfo.skipDoubleShadow(this)) {
            super.onDraw(canvas);
        } else {
            getPaint().setShadowLayer(mShadowInfo.ambientShadowBlur,
                    0f, 0f, mShadowInfo.ambientShadowColor);
            super.onDraw(canvas);

            getPaint().setShadowLayer(mShadowInfo.keyShadowBlur,
                    0f, mShadowInfo.keyShadowOffset, mShadowInfo.keyShadowColor);
            super.onDraw(canvas);
        }
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
        if (Float.isNaN(mTextSize)) {
            mTextSize = getTextSize();
        } else {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
        }

        int w = getWidth() - getTotalPaddingLeft() - getTotalPaddingRight();
        float ratio = Math.min(1f, w / getPaint().measureText(getText().toString()));
        setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize * Math.max(MIN_SHRINK, ratio));
    }
}
