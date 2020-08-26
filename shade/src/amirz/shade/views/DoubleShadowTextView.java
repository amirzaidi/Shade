package amirz.shade.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.launcher3.views.DoubleShadowBubbleTextView;

import java.util.Objects;

@SuppressLint("AppCompatCustomView")
public class DoubleShadowTextView extends TextView {
    private static final float NO_OFFSET = 0f;

    public final DoubleShadowBubbleTextView.ShadowInfo mShadowInfo;

    public final Paint mPaint;
    public CharSequence mText;

    public DoubleShadowTextView(Context context) {
        this(context, null);
    }

    public DoubleShadowTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleShadowTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        mShadowInfo = new DoubleShadowBubbleTextView.ShadowInfo(context, attrs, defStyleAttr);

        setShadowLayer(Math.max(mShadowInfo.keyShadowBlur + mShadowInfo.keyShadowOffset,
                mShadowInfo.ambientShadowBlur), NO_OFFSET, NO_OFFSET, mShadowInfo.keyShadowColor);
    }

    public void updateText(CharSequence text) {
        if (!Objects.equals(text, mText)) {
            mText = text;
            setText(text);
        }
    }

    public void onDraw(Canvas canvas) {
        if (mShadowInfo.skipDoubleShadow(this) && false) {
            super.onDraw(canvas);
            return;
        }

        getPaint().setShadowLayer(mShadowInfo.ambientShadowBlur,
                NO_OFFSET, NO_OFFSET, mShadowInfo.ambientShadowColor);
        super.onDraw(canvas);

        getPaint().setShadowLayer(mShadowInfo.keyShadowBlur,
                NO_OFFSET, mShadowInfo.keyShadowOffset, mShadowInfo.keyShadowColor);
        super.onDraw(canvas);
    }
}
