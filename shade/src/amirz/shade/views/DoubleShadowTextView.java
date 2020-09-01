package amirz.shade.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;

import com.android.launcher3.R;
import com.android.launcher3.views.DoubleShadowBubbleTextView;

public class DoubleShadowTextView extends AppCompatTextView {
    public final Paint mPaint;
    private AutoUpdateTextClock mDate;
    public DoubleShadowBubbleTextView.ShadowInfo mShadowInfo;
    private TextView mForwardEvents;

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
        initDefaultShadowLayer();

        // Might replace this with an attribute later.
        if (getContentDescription() != null && context.getString(R.string.date_content_description)
                    .equals(getContentDescription().toString())) {
            mDate = new AutoUpdateTextClock(this, null);
        }
    }

    private void initDefaultShadowLayer() {
        setShadowLayer(Math.max(mShadowInfo.keyShadowBlur + mShadowInfo.keyShadowOffset,
                mShadowInfo.ambientShadowBlur), 0f, 0f, mShadowInfo.keyShadowColor);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mDate != null) {
            mDate.registerReceiver();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mDate != null) {
            mDate.unregisterReceiver();
        }
    }

    // Used by AutoUpdateTextClock.
    public void updateText(CharSequence text) {
        setText(text);
        setContentDescription(text);
    }

    public void onDraw(Canvas canvas) {
        if (mShadowInfo.skipDoubleShadow(this)) {
            super.onDraw(canvas);
            return;
        }

        getPaint().setShadowLayer(mShadowInfo.ambientShadowBlur, 0f, 0f,
                mShadowInfo.ambientShadowColor);
        super.onDraw(canvas);

        getPaint().setShadowLayer(mShadowInfo.keyShadowBlur, 0f, mShadowInfo.keyShadowOffset,
                mShadowInfo.keyShadowColor);
        super.onDraw(canvas);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mForwardEvents != null) {
            boolean onTouchConsumed = mForwardEvents.onTouchEvent(event);
            if (!onTouchConsumed) {
                return false;
            }
        }
        return super.onTouchEvent(event);
    }

    public DoubleShadowTextView cloneTextView(TextView tv) {
        DoubleShadowTextView dstv = new DoubleShadowTextView(getContext());
        if (tv instanceof TextClock) {
            TextClock tc = (TextClock) tv;
            dstv.mDate = new AutoUpdateTextClock(dstv, tc.getFormat24Hour());
        } else {
            dstv.setText(tv.getText());
        }
        dstv.mShadowInfo = mShadowInfo;
        dstv.initDefaultShadowLayer();
        dstv.mForwardEvents = tv;
        dstv.setOnClickListener(v -> tv.performClick());
        dstv.setTextSize(TypedValue.COMPLEX_UNIT_PX, tv.getTextSize());
        int minPadding = getContext().getResources()
                .getDimensionPixelSize(R.dimen.text_vertical_padding);
        dstv.setPadding(tv.getPaddingLeft(), Math.max(tv.getPaddingTop(), minPadding),
                tv.getPaddingRight(), Math.max(tv.getPaddingBottom(), minPadding));
        dstv.setLetterSpacing(getLetterSpacing());
        dstv.setTextColor(getTextColors());
        dstv.setMaxLines(getMaxLines());
        dstv.setEllipsize(getEllipsize());
        dstv.setTypeface(getTypeface());
        dstv.setHorizontallyScrolling(true);
        return dstv;
    }
}
