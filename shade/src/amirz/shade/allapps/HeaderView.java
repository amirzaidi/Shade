package amirz.shade.allapps;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.FloatingHeaderView;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.anim.PropertySetter;
import com.android.quickstep.AnimatedFloat;

public class HeaderView extends FloatingHeaderView implements Insettable {
    private static final Property<HeaderView, Float> CONTENT_ALPHA =
        new Property<HeaderView, Float>(Float.class, "contentAlpha") {
            @Override
            public Float get(HeaderView predictionsFloatingHeader) {
                return predictionsFloatingHeader.mContentAlpha;
            }

            @Override
            public void set(HeaderView object, Float value) {
                object.mContentAlpha = value;
                object.mTabLayout.setAlpha(value);
            }
        };

    private final int mTopPadding;
    private CategoriesView mCategories;
    private float mContentAlpha = 1f;

    public HeaderView(@NonNull Context context) {
        this(context, null);
    }

    public HeaderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mTopPadding = context.getResources().getDimensionPixelSize(R.dimen.all_apps_header_top_padding);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mCategories = findViewById(R.id.all_apps_categories);
    }

    @Override
    public void setup(AllAppsContainerView.AdapterHolder[] mAH, boolean tabsHidden) {
        mCategories.headerView = this;
        mTabsHidden = tabsHidden;
        loadDivider();
        super.setup(mAH, tabsHidden);
    }

    @Override
    public void setContentVisibility(boolean hasHeader, boolean hasContent, PropertySetter setter,
                                     Interpolator fadeInterpolator) {
        super.setContentVisibility(hasHeader, hasContent, setter, fadeInterpolator);
        if (hasHeader && !hasContent) {
            Launcher.getLauncher(getContext()).getAppsView().getSearchUiManager().resetSearch();
        }

        allowTouchForwarding(hasContent);
        setter.setFloat(this, CONTENT_ALPHA, hasContent ? 1f : 0f, Interpolators.LINEAR);

        int toAlpha = 0;
        if (!hasHeader) {
            toAlpha = mCategories.currentAlpha;
        } else if (hasContent) {
            toAlpha = mCategories.initialAlpha;
        }

        if (mCategories.getAlpha() > 0f) {
            setter.setInt(mCategories, CategoriesView.TEXT_ALPHA, toAlpha, Interpolators.LINEAR);
        } else {
            mCategories.setNewAlpha(toAlpha);
        }

        setter.setFloat(mCategories.scrollYDisabler, AnimatedFloat.VALUE, (hasHeader && !hasContent) ? 1f : 0f, Interpolators.LINEAR);
        setter.setFloat(mCategories.animatedAlpha, AnimatedFloat.VALUE, hasHeader ? 1f : 0f, Interpolators.LINEAR);
    }

    @Override
    public void setInsets(Rect insets) {
        DeviceProfile profile = Launcher.getLauncher(getContext()).getDeviceProfile();
        int leftRightPadding = profile.desiredWorkspaceLeftRightMarginPx
                + profile.cellLayoutPaddingLeftRightPx;
        mCategories.setPadding(leftRightPadding, mCategories.getPaddingTop(),
                leftRightPadding, mCategories.getPaddingBottom());
    }

    public final void updateLayout() {
        int oldMaxTranslation = mMaxTranslation;
        loadDivider();
        if (mMaxTranslation != oldMaxTranslation) {
            Launcher.getLauncher(getContext()).getAppsView().setupHeader();
        }
    }

    @Override
    protected void applyScroll(int uncappedY, int currentY) {
        if (uncappedY < currentY - mTopPadding) {
            mCategories.setHidden(true);
        } else {
            mCategories.setHidden(false);
            mCategories.scrollY = uncappedY;
            mCategories.updateScroll();
        }
    }

    @Override
    public int getMaxTranslation() {
        if (mMaxTranslation == 0 && mTabsHidden) {
            return getResources().getDimensionPixelSize(R.dimen.all_apps_search_bar_bottom_padding);
        }
        if (mMaxTranslation <= 0 || !mTabsHidden) {
            return mMaxTranslation;
        }
        return mMaxTranslation + getPaddingTop();
    }

    private void loadDivider() {
        mCategories.dividerType = mTabsHidden
                ? CategoriesView.DividerType.LINE
                : CategoriesView.DividerType.NONE;

        int dimensionPixelSize = mTabsHidden
                ? getResources().getDimensionPixelSize(R.dimen.all_apps_prediction_row_divider_height)
                : 0;

        mCategories.setPadding(mCategories.getPaddingLeft(), mCategories.getPaddingTop(),
                mCategories.getPaddingRight(), dimensionPixelSize);

        mMaxTranslation = mCategories.getExpectedHeight();
    }

    public void showCategories(boolean show) {
        mCategories.setVisibility(show ? View.VISIBLE : View.GONE);
        updateLayout();
    }
}
