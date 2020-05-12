package amirz.shade.search;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.qsb.QsbContainerView;
import com.android.launcher3.qsb.QsbWidgetHostView;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.Themes;

import java.util.ArrayList;

import amirz.shade.customization.DockSearch;
import amirz.shade.hidden.HiddenAppsSearchAlgorithm;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.getSize;
import static android.view.View.MeasureSpec.makeMeasureSpec;
import static com.android.launcher3.LauncherState.ALL_APPS_CONTENT;
import static com.android.launcher3.Utilities.prefixTextWithIcon;
import static com.android.launcher3.icons.IconNormalizer.ICON_VISIBLE_AREA_FACTOR;

public class AllAppsQsb extends QsbContainerView
        implements Insettable, SearchUiManager,
        AllAppsSearchBarController.Callbacks, AllAppsStore.OnUpdateListener {
    private final Launcher mLauncher;
    private final AllAppsSearchBarController mSearchBarController;
    private final SpannableStringBuilder mSearchQueryBuilder;

    private AlphabeticalAppsList mApps;
    private AllAppsContainerView mAppsView;

    // This value was used to position the QSB. We store it here for translationY animations.
    private final float mFixedTranslationY;
    private final float mMarginTopAdjusting;
    private final int mMinTopInset;

    // Delegate views.
    private View mSearchWrapperView;
    private FrameLayout mFallbackSearchView;
    private EditText mFallbackSearchViewText;

    private boolean mSearchRequested;
    private final int[] currentPadding = new int[2];

    public static class HotseatQsbFragment extends QsbFragment {
        @Override
        public boolean isQsbEnabled() {
            return true;
        }

        @Override
        protected QsbWidgetHost createHost() {
            return new QsbWidgetHost(getContext(), QSB_WIDGET_HOST_ID,
                    (c) -> new QsbWidgetHostView(c));
        }

        @Override
        protected AppWidgetProviderInfo getSearchWidgetProvider() {
            return DockSearch.getWidgetInfo(getContext());
        }
    }

    public AllAppsQsb(Context context) {
        this(context, null);
    }

    public AllAppsQsb(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsQsb(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mLauncher = Launcher.getLauncher(context);
        mSearchBarController = new AllAppsSearchBarController();

        mSearchQueryBuilder = new SpannableStringBuilder();
        Selection.setSelection(mSearchQueryBuilder, 0);

        mFixedTranslationY = getTranslationY();
        mMarginTopAdjusting = mFixedTranslationY - getPaddingTop();
        mMinTopInset = context.getResources().getDimensionPixelSize(R.dimen.all_apps_min_top_inset);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mLauncher.getAppsView().getAppsStore().addUpdateListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mLauncher.getAppsView().getAppsStore().removeUpdateListener(this);
    }

    private boolean shouldHideDockSearch() {
        return DockSearch.getWidgetInfo(mLauncher) == null;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSearchWrapperView = findViewById(R.id.search_wrapper_view);
        mSearchWrapperView.setVisibility(shouldHideDockSearch()
                ? View.GONE
                : View.VISIBLE);

        mFallbackSearchView = findViewById(R.id.fallback_search_view);
        mFallbackSearchViewText = findViewById(R.id.fallback_search_view_text);

        mFallbackSearchView.setVisibility(View.INVISIBLE);

        Context context = getContext();

        /*
        RippleDrawable bg = (RippleDrawable) mFallbackSearchView.getBackground();
        GradientDrawable gd = (GradientDrawable) bg.findDrawableByLayerId(R.id.search_basic);

        if (Utilities.ATLEAST_Q) {
            // The corners should be 3x as curved as the dialog curve.
            gd.setCornerRadius(gd.getCornerRadius() * 3f);
        }

        int bgColor = Themes.getAttrColor(context, R.attr.shadeColorSearchBar);
        int overlay = Themes.getAttrColor(context, R.attr.shadeColorAllAppsOverlay);
        if (ColorUtils.setAlphaComponent(overlay, 0) != overlay) {
            boolean isDark = Themes.getAttrBoolean(context, R.attr.isMainColorDark);

            // Alpha is not zero, so update it to the right value.
            overlay = ColorUtils.setAlphaComponent(overlay,
                    context.getResources().getInteger(isDark
                            ? R.integer.shade_all_apps_dark_alpha
                            : R.integer.shade_all_apps_light_alpha));
            bgColor = ColorUtils.compositeColors(overlay, bgColor);
        }
        gd.setColor(bgColor);

         */

        mFallbackSearchViewText.setSpannedHint(
                prefixTextWithIcon(context,
                        R.drawable.ic_allapps_search,
                        mFallbackSearchViewText.getHint()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // Update the width to match the grid padding
        int myRequestedWidth = getSize(widthMeasureSpec);
        int myRequestedHeight = getSize(heightMeasureSpec);

        DeviceProfile dp = mLauncher.getDeviceProfile();

        int rowWidth = myRequestedWidth - mAppsView.getActiveRecyclerView().getPaddingLeft()
                - mAppsView.getActiveRecyclerView().getPaddingRight();

        int cellWidth = DeviceProfile.calculateCellWidth(rowWidth, dp.inv.numHotseatIcons);
        int iconVisibleSize = Math.round(ICON_VISIBLE_AREA_FACTOR * dp.iconSizePx);
        int iconPadding = cellWidth - iconVisibleSize;

        int myWidth = rowWidth - iconPadding + getPaddingLeft() + getPaddingRight();

        Resources res = getResources();
        int widgetPad = res.getDimensionPixelSize(R.dimen.qsb_widget_padding);

        mFallbackSearchView.measure(makeMeasureSpec(myWidth + 2 * widgetPad, EXACTLY),
                makeMeasureSpec(myRequestedHeight + widgetPad, EXACTLY));

        currentPadding[0] = 0;
        currentPadding[1] = 0;
        calcPaddingRecursive(mSearchWrapperView, 2);

        mSearchWrapperView.setPadding(
                mSearchWrapperView.getPaddingLeft() + widgetPad - currentPadding[0],
                mSearchWrapperView.getPaddingTop(),
                mSearchWrapperView.getPaddingRight() + widgetPad - currentPadding[1],
                mSearchWrapperView.getPaddingBottom());

        mSearchWrapperView.measure(makeMeasureSpec(myWidth + 2 * widgetPad, EXACTLY),
                makeMeasureSpec(myRequestedHeight, EXACTLY));
    }

    private void calcPaddingRecursive(View view, int lvl) {
        currentPadding[0] += view.getPaddingLeft();
        currentPadding[1] += view.getPaddingRight();
        if (view instanceof ViewGroup && lvl > 0) {
            ViewGroup group = (ViewGroup) view;
            if (group.getChildCount() == 1) {
                calcPaddingRecursive(group.getChildAt(0), lvl - 1);
            }
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Shift the widget horizontally so that its centered in the parent (b/63428078)
        View parent = (View) getParent();
        int availableWidth = parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight();
        int myWidth = right - left;
        int expectedLeft = parent.getPaddingLeft() + (availableWidth - myWidth) / 2;
        int shift = expectedLeft - left;
        setTranslationX(shift);
    }

    @Override
    public void initialize(AllAppsContainerView appsView) {
        mApps = appsView.getApps();
        mAppsView = appsView;
        mSearchBarController.initialize(
                new HiddenAppsSearchAlgorithm(mLauncher, appsView.getAppsStore().getApps()),
                mFallbackSearchViewText, mLauncher, this);

        appsView.setRecyclerViewVerticalFadingEdgeEnabled(true);
    }

    @Override
    public void onAppsUpdated() {
        mSearchBarController.refreshSearchResult();
    }

    @Override
    public void resetSearch() {
        mSearchBarController.reset();
    }

    @Override
    public void preDispatchKeyEvent(KeyEvent event) {
        // Determine if the key event was actual text, if so, focus the search bar and then dispatch
        // the key normally so that it can process this key event
        if (!mSearchBarController.isSearchFieldFocused() &&
                event.getAction() == KeyEvent.ACTION_DOWN) {
            final int unicodeChar = event.getUnicodeChar();
            final boolean isKeyNotWhitespace = unicodeChar > 0 &&
                    !Character.isWhitespace(unicodeChar) && !Character.isSpaceChar(unicodeChar);
            if (isKeyNotWhitespace) {
                boolean gotKey = TextKeyListener.getInstance().onKeyDown(this, mSearchQueryBuilder,
                        event.getKeyCode(), event);
                if (gotKey && mSearchQueryBuilder.length() > 0) {
                    mSearchBarController.focusSearchField();
                }
            }
        }
    }

    @Override
    public void onSearchResult(String query, ArrayList<ComponentKey> apps) {
        if (apps != null) {
            mApps.setOrderedFilter(apps);
            notifyResultChanged();
            mAppsView.setLastSearchQuery(query);
        }
    }

    @Override
    public void clearSearchResult() {
        if (mApps.setOrderedFilter(null)) {
            notifyResultChanged();
        }

        // Clear the search query
        mSearchQueryBuilder.clear();
        mSearchQueryBuilder.clearSpans();
        Selection.setSelection(mSearchQueryBuilder, 0);
        mAppsView.onClearSearchResult();
    }

    private void notifyResultChanged() {
        mAppsView.onSearchResultsChanged();
        mAppsView.getFloatingHeaderView().setCollapsed(mApps.hasNoFilteredResults());
    }

    @Override
    public void setInsets(Rect insets) {
        MarginLayoutParams mlp = (MarginLayoutParams) getLayoutParams();
        int topInset = Math.max(mMinTopInset, insets.top);
        mlp.topMargin = Math.round(Math.max(
                -mFixedTranslationY, topInset - mMarginTopAdjusting));
        requestLayout();
    }

    @Override
    public float getScrollRangeDelta(Rect insets) {
        if (mLauncher.getDeviceProfile().isVerticalBarLayout() || shouldHideDockSearch()) {
            return 0;
        } else {
            int topInset = Math.max(mMinTopInset, insets.top);
            int topMargin = Math.round(Math.max(
                    -mFixedTranslationY, topInset - mMarginTopAdjusting));

            DeviceProfile dp = mLauncher.getWallpaperDeviceProfile();
            int searchPadding = getLayoutParams().height;
            int hotseatPadding = dp.hotseatBarBottomPaddingPx - searchPadding;

            return insets.bottom + topMargin + mFixedTranslationY + searchPadding
                    + hotseatPadding * 0.65f;
        }
    }

    @Override
    public void setContentVisibility(int visibleElements, PropertySetter setter,
                                     Interpolator interpolator) {
        boolean showAllApps = (visibleElements & ALL_APPS_CONTENT) != 0;
        setter.setViewAlpha(mSearchWrapperView,
                showAllApps || shouldHideDockSearch() ? 0f : 1f, Interpolators.LINEAR);
        setter.setViewAlpha(mFallbackSearchView,
                showAllApps ? 1f : 0f, Interpolators.LINEAR);
    }

    public void requestSearch() {
        mSearchRequested = true;
    }

    public void showKeyboardOnSearchRequest() {
        if (mSearchRequested) {
            mSearchRequested = false;
            mFallbackSearchViewText.showKeyboard();
        }
    }

    public boolean tryClearSearch() {
        if (mFallbackSearchViewText.length() > 0) {
            mAppsView.reset(true);
            mAppsView.requestFocus();
            return true;
        }
        return false;
    }
}
