package amirz.shade.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.android.launcher3.R;
import com.android.launcher3.views.DoubleShadowBubbleTextView;

import java.util.Objects;

public class DoubleShadowTextView extends AppCompatTextView {
    private static final float NO_OFFSET = 0f;

    public final Paint mPaint;
    public DoubleShadowBubbleTextView.ShadowInfo mShadowInfo;
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
        if (mShadowInfo.skipDoubleShadow(this)) {
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

    public DoubleShadowTextView cloneTextView(TextView tv) {
        DoubleShadowTextView dstv = new DoubleShadowTextView(getContext());
        dstv.mShadowInfo = mShadowInfo;
        dstv.updateText(tv.getText());
        dstv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize());
        int minPadding = getContext().getResources()
                .getDimensionPixelSize(R.dimen.text_vertical_padding);
        dstv.setPadding(tv.getPaddingLeft(), Math.max(tv.getPaddingTop(), minPadding),
                tv.getPaddingRight(), Math.max(tv.getPaddingBottom(), minPadding));
        dstv.setLetterSpacing(getLetterSpacing());
        dstv.setTextColor(getTextColors());
        dstv.setMaxLines(getMaxLines());
        dstv.setEllipsize(getEllipsize());
        dstv.setHorizontallyScrolling(isHorizontallyScrollable());
        dstv.setTypeface(getTypeface());
        return dstv;
    }
}
