package amirz.shade.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.launcher3.util.Themes;
import com.android.quickstep.views.ShelfScrimView;

public class GradientScrimView extends ShelfScrimView {
    private static final int ALPHA_MASK_HEIGHT_DP = 500;
    private static final int ALPHA_MASK_WIDTH_DP = 2;

    private final Bitmap mAlphaGradientMask;

    private int mThemeScrimColor;
    private int mThemeScrimColorFoot;

    private int mWidth;
    private int mHeight;
    private final RectF mAlphaMaskRect = new RectF();
    private final RectF mFinalMaskRect = new RectF();
    private final Paint mPaintWithScrim = new Paint();
    private final int mMaskHeight, mMaskWidth;

    public GradientScrimView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mThemeScrimColor = Themes.getAttrColor(context, R.attr.allAppsScrimColor);
        mThemeScrimColorFoot = Themes.getAttrColor(context, R.attr.allAppsScrimColorFoot);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mMaskHeight = Utilities.pxFromDp(ALPHA_MASK_HEIGHT_DP, dm);
        mMaskWidth = Utilities.pxFromDp(ALPHA_MASK_WIDTH_DP, dm);
        mAlphaGradientMask = createDitheredAlphaMask();
    }

    private void createRadialShader() {
        final float gradientCenterY = 1.05f;
        float radius = Math.max(mHeight, mWidth) * gradientCenterY;
        float posScreenBottom = (radius - mHeight) / radius; // center lives below screen

        int alpha = getResources().getInteger(R.integer.shade_gradient_alpha);
        int colorHead = ColorUtils.setAlphaComponent(mThemeScrimColor, alpha);
        int colorFoot = ColorUtils.setAlphaComponent(mThemeScrimColorFoot, alpha);
        mPaintWithScrim.setShader(new RadialGradient(
                mWidth * 0.5f,
                mHeight * gradientCenterY,
                radius,
                new int[] { colorFoot, colorFoot, colorHead },
                new float[] {0f, posScreenBottom, 1f},
                Shader.TileMode.CLAMP));
    }

    private Bitmap createDitheredAlphaMask() {
        Bitmap dst = Bitmap.createBitmap(mMaskWidth, mMaskHeight, Bitmap.Config.ALPHA_8);
        Canvas c = new Canvas(dst);
        Paint paint = new Paint(Paint.DITHER_FLAG);
        LinearGradient lg = new LinearGradient(0, 0, 0, mMaskHeight,
                new int[]{
                        0x00FFFFFF,
                        ColorUtils.setAlphaComponent(Color.WHITE, (int) (0xFF * 0.95)),
                        0xFFFFFFFF},
                new float[] { 0f, 0.8f, 1f },
                Shader.TileMode.CLAMP);
        paint.setShader(lg);
        c.drawRect(0, 0, mMaskWidth, mMaskHeight, paint);
        return dst;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        if (mWidth + mHeight > 0) {
            createRadialShader();
        }
    }

    @Override
    public void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo) {
        super.onExtractedColorsChanged(wallpaperColorInfo);
        if (mWidth + mHeight > 0) {
            createRadialShader();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mThemeScrimColorFoot == 0) {
            super.onDraw(canvas);
        } else {
            float linearProgress = 1.f - mProgress;
            float startMaskY = (1f - linearProgress) * mHeight - mMaskHeight * linearProgress;
            mPaintWithScrim.setAlpha(255);
            float div = (float) Math.floor(startMaskY + mMaskHeight);
            mAlphaMaskRect.set(0, startMaskY, mWidth, div);
            mFinalMaskRect.set(0, div, mWidth, mHeight);
            canvas.drawBitmap(mAlphaGradientMask, null, mAlphaMaskRect, mPaintWithScrim);
            canvas.drawRect(mFinalMaskRect, mPaintWithScrim);
        }
    }
}
