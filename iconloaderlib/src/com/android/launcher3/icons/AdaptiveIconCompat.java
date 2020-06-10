package com.android.launcher3.icons;

import android.graphics.*;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.O)
public class AdaptiveIconCompat extends AdaptiveIconDrawable {
    /**
     * Mask path is defined inside device configuration in following dimension: [100 x 100]
     */
    private static final float MASK_SIZE = 100f;

    /**
     * All four sides of the layers are padded with extra inset so as to provide
     * extra content to reveal within the clip path when performing affine transformations on the
     * layers.
     *
     * Each layers will reserve 25% of it's width and height.
     *
     * As a result, the view port of the layers is smaller than their intrinsic width and height.
     */
    private static final float EXTRA_INSET_PERCENTAGE = 1 / 4f;
    private static final float DEFAULT_VIEW_PORT_SCALE = 1f / (1 + 2 * EXTRA_INSET_PERCENTAGE);

    /**
     * Clip path defined in R.string.config_icon_mask.
     */
    private static Path sMask;

    public static void setMask(Path mask) {
        sMask = mask;
    }

    public static AdaptiveIconDrawable wrap(AdaptiveIconDrawable in) {
        return sMask == null || in instanceof AdaptiveIconCompat
                ? in
                : new AdaptiveIconCompat(in);
    }

    /**
     * Scaled mask based on the view bounds.
     */
    private final Path mMask;
    private final Path mMaskScaleOnly;
    private final Matrix mMaskMatrix = new Matrix();
    private final Region mTransparentRegion = new Region();

    private Shader mLayersShader;
    private Bitmap mLayersBitmap;

    private final Rect mTmpOutRect = new Rect();

    private boolean mSuspendChildInvalidation;
    private boolean mChildRequestedInvalidation;
    private final Canvas mCanvas = new Canvas();
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG |
            Paint.FILTER_BITMAP_FLAG);

    private AdaptiveIconCompat(AdaptiveIconDrawable drawable) {
        super(drawable.getBackground(), drawable.getForeground());
        mMask = new Path(sMask);
        mMaskScaleOnly = new Path(mMask);
        setAlpha(drawable.getAlpha());
        setBounds(drawable.getBounds());
        setChangingConfigurations(drawable.getChangingConfigurations());
    }

    @Override
    public void setAlpha(int alpha) {
        super.setAlpha(255);
        mPaint.setAlpha(alpha);
    }

    @Override
    public int getAlpha() {
        return mPaint.getAlpha();
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public Path getIconMask() {
        return mMask;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (bounds.isEmpty()) {
            return;
        }
        updateLayerBounds(bounds);
    }

    private void updateLayerBounds(Rect bounds) {
        if (bounds.isEmpty()) {
            return;
        }
        try {
            suspendChildInvalidation();
            updateLayerBoundsInternal(bounds);
            updateMaskBoundsInternal(bounds);
        } finally {
            resumeChildInvalidation();
        }
    }

    /**
     * Set the child layer bounds bigger than the view port size by {@link #DEFAULT_VIEW_PORT_SCALE}
     */
    private void updateLayerBoundsInternal(Rect bounds) {
        int cX = bounds.width() / 2;
        int cY = bounds.height() / 2;

        Drawable[] dArray = { getBackground(), getForeground() };
        for (Drawable d : dArray) {
            if (d == null) {
                continue;
            }

            int insetWidth = (int) (bounds.width() / (DEFAULT_VIEW_PORT_SCALE * 2));
            int insetHeight = (int) (bounds.height() / (DEFAULT_VIEW_PORT_SCALE * 2));
            final Rect outRect = mTmpOutRect;
            outRect.set(cX - insetWidth, cY - insetHeight, cX + insetWidth, cY + insetHeight);

            d.setBounds(outRect);
        }
    }

    private void updateMaskBoundsInternal(Rect b) {
        // reset everything that depends on the view bounds
        mMaskMatrix.setScale(b.width() / MASK_SIZE, b.height() / MASK_SIZE);
        sMask.transform(mMaskMatrix, mMaskScaleOnly);

        mMaskMatrix.postTranslate(b.left, b.top);
        sMask.transform(mMaskMatrix, mMask);

        if (mLayersBitmap == null || mLayersBitmap.getWidth() != b.width()
                || mLayersBitmap.getHeight() != b.height()) {
            mLayersBitmap = Bitmap.createBitmap(b.width(), b.height(), Bitmap.Config.ARGB_8888);
        }

        mPaint.setShader(null);
        mTransparentRegion.setEmpty();
        mLayersShader = null;
    }

    @Override
    public void draw(Canvas canvas) {
        if (mLayersBitmap == null) {
            return;
        }
        if (mLayersShader == null) {
            mCanvas.setBitmap(mLayersBitmap);
            mCanvas.drawColor(Color.BLACK);
            Drawable[] dArray = { getBackground(), getForeground() };
            for (Drawable d : dArray) {
                if (d != null) {
                    d.draw(mCanvas);
                }
            }
            mLayersShader = new BitmapShader(mLayersBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
            mPaint.setShader(mLayersShader);
        }
        if (mMaskScaleOnly != null) {
            Rect bounds = getBounds();
            canvas.translate(bounds.left, bounds.top);
            canvas.drawPath(mMaskScaleOnly, mPaint);
            canvas.translate(-bounds.left, -bounds.top);
        }
    }

    @Override
    public void invalidateSelf() {
        mLayersShader = null;
        super.invalidateSelf();
    }

    @Override
    public void getOutline(@NonNull Outline outline) {
        outline.setConvexPath(mMask);
    }

    /**
     * Temporarily suspends child invalidation.
     *
     * @see #resumeChildInvalidation()
     */
    private void suspendChildInvalidation() {
        mSuspendChildInvalidation = true;
    }

    /**
     * Resumes child invalidation after suspension, immediately performing an
     * invalidation if one was requested by a child during suspension.
     *
     * @see #suspendChildInvalidation()
     */
    private void resumeChildInvalidation() {
        mSuspendChildInvalidation = false;

        if (mChildRequestedInvalidation) {
            mChildRequestedInvalidation = false;
            invalidateSelf();
        }
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        if (mSuspendChildInvalidation) {
            mChildRequestedInvalidation = true;
        } else {
            invalidateSelf();
        }
    }

    @Override
    public Region getTransparentRegion() {
        if (mTransparentRegion.isEmpty()) {
            mMask.toggleInverseFillType();
            mTransparentRegion.set(getBounds());
            mTransparentRegion.setPath(mMask, mTransparentRegion);
            mMask.toggleInverseFillType();
        }
        return mTransparentRegion;
    }
}

