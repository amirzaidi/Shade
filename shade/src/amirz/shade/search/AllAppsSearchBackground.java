package amirz.shade.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.BaseRecyclerView;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.icons.ShadowGenerator;

public class AllAppsSearchBackground extends FrameLayout implements View.OnClickListener {
    private Bitmap mShadowBitmap;
    private Bitmap mBaseBitmap;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mDestRect = new RectF();
    private final Rect mSrcRect = new Rect();

    private EditText mEditText;
    private float mRadius = Float.NaN;

    // ToDo: Set during runtime.
    private int mColor;
    private int mShadowAlpha;

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
            setShadowAlpha(((BaseRecyclerView) recyclerView).getCurrentScrollY());
        }
    };

    public AllAppsSearchBackground(@NonNull Context context) {
        this(context, null);
    }

    public AllAppsSearchBackground(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsSearchBackground(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnClickListener(this);
    }

    public void onClick(View v) {
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        return mEditText.onTouchEvent(ev);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mEditText = findViewById(R.id.fallback_search_view_text);
    }

    @Override
    public void draw(Canvas canvas) {
        if (mShadowAlpha > 0) {
            if (mShadowBitmap == null) {
                Resources res = getResources();
                mShadowBitmap = createBitmap(
                        res.getDimension(R.dimen.hotseat_qsb_scroll_shadow_blur_radius),
                        res.getDimension(R.dimen.hotseat_qsb_scroll_key_shadow_offset), 0);
            }
            mPaint.setAlpha(mShadowAlpha);
            drawOnCanvas(mShadowBitmap, canvas);
            mPaint.setAlpha(255);
        }

        if (mBaseBitmap == null) {
            int iconBitmapSize = LauncherAppState.getIDP(getContext()).iconBitmapSize;
            mBaseBitmap = createBitmap(iconBitmapSize / 96f, iconBitmapSize / 48f, mColor);
        }

        drawOnCanvas(mBaseBitmap, canvas);
        super.draw(canvas);
    }

    protected void drawOnCanvas(Bitmap bitmap, Canvas canvas) {
        int layoutHeight = getHeight() - getPaddingTop() - getPaddingBottom();
        int layoutWidth = layoutHeight + 20;
        int bmWidth = bitmap.getWidth();
        int bmHeight = bitmap.getHeight();

        mSrcRect.top = 0;
        mSrcRect.bottom = bmHeight;
        mDestRect.top = getPaddingTop() - (bmHeight - layoutHeight) / 2f;
        mDestRect.bottom = bmHeight + mDestRect.top;
        float horizontalMargin = (bmWidth - layoutWidth) / 2f;
        int bmMidX = bmWidth / 2;

        // Left part.
        float leftStart = getPaddingLeft() - horizontalMargin;
        drawWithDimensions(bitmap, canvas, 0, bmMidX + 1, leftStart, leftStart + bmMidX + 1);

        // Right part.
        float rightEnd = getWidth() - getPaddingRight() + horizontalMargin;
        drawWithDimensions(bitmap, canvas, bmMidX - 1, bmWidth, rightEnd - bmMidX - 1, rightEnd);

        // Middle part.
        drawWithDimensions(bitmap, canvas, bmMidX - 5, bmMidX + 5, leftStart + bmMidX, rightEnd - bmMidX);
    }

    private void drawWithDimensions(Bitmap bitmap, Canvas canvas, int srcLeft, int srcRight,
                                    float destLeft, float destRight) {
        mSrcRect.left = srcLeft;
        mSrcRect.right = srcRight;
        mDestRect.left = destLeft;
        mDestRect.right = destRight;
        canvas.drawBitmap(bitmap, mSrcRect, mDestRect, mPaint);
    }

    private Bitmap createBitmap(float shadowBlur, float keyShadowDistance, int color) {
        int height = getHeight() - getPaddingTop() - getPaddingBottom();
        int width = height + 20;
        ShadowGenerator.Builder builder = new ShadowGenerator.Builder(color);
        builder.shadowBlur = shadowBlur;
        builder.keyShadowDistance = keyShadowDistance;
        builder.keyShadowAlpha = builder.ambientShadowAlpha;
        Bitmap pill = builder.createPill(width, height,
                Float.isNaN(mRadius) ? height / 2f : mRadius);
        if (Color.alpha(color) < 0xFF) {
            Canvas canvas = new Canvas(pill);
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            canvas.drawRoundRect(builder.bounds, height / 2f, height / 2f, paint);
            paint.setXfermode(null);
            paint.setColor(color);
            canvas.drawRoundRect(builder.bounds, height / 2f, height / 2f, paint);
            canvas.setBitmap(null);
        }
        if (Utilities.ATLEAST_OREO) {
            return pill.copy(Bitmap.Config.HARDWARE, false);
        }
        return pill;
    }

    public void setColor(int color) {
        if (mColor != color) {
            mColor = color;
            mBaseBitmap = null;
            invalidate();
        }
    }

    public void setRadius(float radius) {
        if (mRadius != radius) {
            mRadius = radius;
            mShadowBitmap = null;
            mBaseBitmap = null;
            invalidate();
        }
    }

    public RecyclerView.OnScrollListener getElevationController() {
        return mOnScrollListener;
    }

    public void setShadowAlpha(int newAlpha) {
        int normalizedAlpha = Utilities.boundToRange(newAlpha, 0, 255);
        if (mShadowAlpha != normalizedAlpha) {
            mShadowAlpha = normalizedAlpha;
            invalidate();
        }
    }
}
