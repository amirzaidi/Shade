package amirz.shade.allapps;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Process;
import android.support.annotation.Nullable;
import android.support.v4.graphics.ColorUtils;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;

import com.android.launcher3.AppInfo;
import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.graphics.LauncherIcons;
import com.android.launcher3.keyboard.FocusIndicatorHelper;
import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;
import com.android.launcher3.touch.ItemClickHandler;
import com.android.launcher3.touch.ItemLongClickListener;
import com.android.launcher3.util.Themes;
import com.android.quickstep.AnimatedFloat;

import java.util.ArrayList;
import java.util.List;

public class CategoriesView extends LinearLayout
        implements DeviceProfile.OnDeviceProfileChangeListener {
    public enum DividerType {
        NONE,
        LINE
    }

    static final Property<CategoriesView, Integer> TEXT_ALPHA =
            new Property<CategoriesView, Integer>(Integer.class, "textAlpha") {
        @Override
        public void set(CategoriesView object, Integer value) {
            object.setNewAlpha(value);
        }

        @Override
        public Integer get(CategoriesView object) {
            return object.currentAlpha;
        }
    };

    private static final Interpolator sInterpolator = f -> f < 0.8f ? 0.0f : (f - 0.8f) / 0.2f;
    private final FocusIndicatorHelper mFocusHelper;
    private final int mNumColumns;
    private final Launcher mLauncher;
    private final int mTextColor;
    private boolean mHidden;
    private final int mPaintColor;

    private final Paint mPaint;
    final int initialAlpha;
    int currentAlpha;
    float scrollY;
    final AnimatedFloat animatedAlpha;
    final AnimatedFloat scrollYDisabler;
    DividerType dividerType;
    HeaderView headerView;

    public CategoriesView(Context context) {
        this(context, null);
    }

    public CategoriesView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mFocusHelper = new FocusIndicatorHelper.SimpleFocusIndicatorHelper(this);
        mNumColumns = Math.min(5,
                LauncherAppState.getInstance(context).getInvariantDeviceProfile().numColumns);
        mLauncher = Launcher.getLauncher(context);
        mLauncher.addOnDeviceProfileChangeListener(this);

        mPaint = new Paint();
        mPaint.setColor(Themes.getAttrColor(context, android.R.attr.colorControlHighlight));
        mPaint.setStrokeWidth((float) getResources().getDimensionPixelSize(R.dimen.all_apps_divider_height));
        mPaintColor = mPaint.getColor();

        mTextColor = Themes.getAttrColor(context, android.R.attr.textColorSecondary);
        initialAlpha = Color.alpha(mTextColor);
        currentAlpha = initialAlpha;
        animatedAlpha = new AnimatedFloat(this::updateScroll);
        scrollYDisabler = new AnimatedFloat(this::updateScroll);

        setOrientation(LinearLayout.HORIZONTAL);
        setWillNotDraw(false);
    }

    public final void setNewAlpha(int newAlpha) {
        currentAlpha = newAlpha;
        int textColor = ColorUtils.setAlphaComponent(mTextColor, currentAlpha);
        for (int i = 0; i < getChildCount(); i++) {
            ((BubbleTextView) getChildAt(i)).setTextColor(textColor);
        }
        int a = Math.round(((float) (Color.alpha(mPaintColor) * newAlpha)) / 255f);
        if (dividerType != DividerType.NONE
                && ColorUtils.setAlphaComponent(mPaintColor, a) != mPaint.getColor()) {
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (dividerType == DividerType.LINE) {
            int dimensionPixelSize =
                    getResources().getDimensionPixelSize(R.dimen.dynamic_grid_edge_margin);
            float lineHeight = (float) (getHeight() - (getPaddingBottom() / 2));
            canvas.drawLine(
                    (float) (getPaddingLeft() + dimensionPixelSize),
                    lineHeight,
                    (float) ((getWidth() - getPaddingRight()) - dimensionPixelSize),
                    lineHeight,
                    mPaint);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Usually we wait for the apps list to get updated, but since we do not load any app
        // suggestions we can immediately create the views here.
        recreateBubbleTextViews();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,
                MeasureSpec.makeMeasureSpec(getExpectedHeight(), MeasureSpec.EXACTLY));
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mFocusHelper.draw(canvas);
        super.dispatchDraw(canvas);
    }

    public final int getExpectedHeight() {
        if (getVisibility() == View.GONE) {
            return 0;
        }
        return Launcher.getLauncher(getContext()).getDeviceProfile().allAppsCellHeightPx
                + getPaddingTop() + getPaddingBottom();
    }

    @Override
    public void onDeviceProfileChanged(DeviceProfile deviceProfile) {
        removeAllViews();
        recreateBubbleTextViews();
    }

    private void recreateBubbleTextViews() {
        List<AppInfo> shortcuts = CategoryShortcuts.getAll(mLauncher);
        int size = shortcuts.size() - 1;

        while (getChildCount() < Math.min(size, mNumColumns)) {
            BubbleTextView btv = (BubbleTextView) mLauncher.getLayoutInflater()
                    .inflate(R.layout.all_apps_icon, this, false);
            btv.setOnClickListener(ItemClickHandler.INSTANCE);
            btv.setOnFocusChangeListener(mFocusHelper);
            LayoutParams layoutParams = (LayoutParams) btv.getLayoutParams();
            layoutParams.height = mLauncher.getDeviceProfile().allAppsCellHeightPx;
            layoutParams.width = 0;
            layoutParams.weight = 1f;
            addView(btv);
        }

        int textColor = ColorUtils.setAlphaComponent(mTextColor, initialAlpha);
        for (int i = 0; i < getChildCount(); i++) {
            BubbleTextView btv = (BubbleTextView) getChildAt(i);
            btv.reset();
            if (size > i) {
                btv.setVisibility(View.VISIBLE);
                btv.applyFromApplicationInfo(shortcuts.get(i));
                btv.setTextColor(textColor);
            } else {
                btv.setVisibility(size == 0
                        ? View.GONE
                        : View.INVISIBLE);
            }
        }

        headerView.updateLayout();
    }

    public final void setHidden(boolean hidden) {
        mHidden = hidden;
        updateScroll();
    }

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    void updateScroll() {
        setTranslationY((1f - scrollYDisabler.value) * scrollY);
        float interpolation = sInterpolator.getInterpolation(scrollYDisabler.value);
        setAlpha(animatedAlpha.value * (interpolation + ((1f - interpolation) * (mHidden ? 0f : 1f))));
    }
}
