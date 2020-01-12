package amirz.shade.search;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.method.TextKeyListener;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Interpolator;

import com.android.launcher3.DeviceProfile;
import com.android.launcher3.ExtendedEditText;
import com.android.launcher3.Insettable;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;
import com.android.launcher3.allapps.AllAppsStore;
import com.android.launcher3.allapps.AlphabeticalAppsList;
import com.android.launcher3.allapps.FloatingHeaderView;
import com.android.launcher3.allapps.SearchUiManager;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm;
import com.android.launcher3.anim.Interpolators;
import com.android.launcher3.anim.PropertySetter;
import com.android.launcher3.qsb.QsbContainerView;
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;

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

    // Delegate views.
    private View mSearchWrapperView;
    private ExtendedEditText mFallbackSearchView;

    private boolean mSearchRequested;

    public static class HotseatQsbFragment extends QsbFragment {
        @Override
        public boolean isQsbEnabled() {
            return true;
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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSearchWrapperView = findViewById(R.id.search_wrapper_view);
        mFallbackSearchView = findViewById(R.id.fallback_search_view);
        mFallbackSearchView.setVisibility(View.INVISIBLE);

        if (Utilities.ATLEAST_P) {
            // The corners should be 4x as curved as the dialog curve.
            RippleDrawable bg = (RippleDrawable) mFallbackSearchView.getBackground();
            GradientDrawable gd = (GradientDrawable) bg.findDrawableByLayerId(R.id.search_basic);
            gd.setCornerRadius(gd.getCornerRadius() * 4f);
        }

        mFallbackSearchView.setHint(
                prefixTextWithIcon(getContext(),
                        R.drawable.ic_allapps_search,
                        mFallbackSearchView.getHint()));
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

        int widgetPad = getResources().getDimensionPixelSize(R.dimen.qsb_widget_padding);

        mFallbackSearchView.measure(makeMeasureSpec(myWidth, EXACTLY),
                makeMeasureSpec(myRequestedHeight - widgetPad, EXACTLY));

        mSearchWrapperView.measure(makeMeasureSpec(myWidth + 2 * widgetPad, EXACTLY),
                makeMeasureSpec(myRequestedHeight, EXACTLY));
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
        mFallbackSearchView = findViewById(R.id.fallback_search_view);
        mSearchBarController.initialize(
                new DefaultAppSearchAlgorithm(mApps.getApps()), mFallbackSearchView, mLauncher, this);

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
        mlp.topMargin = Math.round(Math.max(-mFixedTranslationY, insets.top - mMarginTopAdjusting));
        requestLayout();
    }

    @Override
    public float getScrollRangeDelta(Rect insets) {
        if (mLauncher.getDeviceProfile().isVerticalBarLayout()) {
            return 0;
        } else {
            int topMargin = Math.round(Math.max(
                    -mFixedTranslationY, insets.top - mMarginTopAdjusting));

            DeviceProfile dp = mLauncher.getWallpaperDeviceProfile();
            int searchPadding = getLayoutParams().height;
            int hotseatPadding = (dp.hotseatBarSizePx - dp.hotseatCellHeightPx) - searchPadding;

            return insets.bottom + topMargin + mFixedTranslationY
                    + searchPadding + (int) ((float) (hotseatPadding - insets.bottom) * 0.45f);
        }
    }

    @Override
    public void setContentVisibility(int visibleElements, PropertySetter setter,
                                     Interpolator interpolator) {
        boolean showAllApps = (visibleElements & ALL_APPS_CONTENT) != 0;
        setter.setViewAlpha(mSearchWrapperView, showAllApps ? 0f : 1f, Interpolators.LINEAR);
        setter.setViewAlpha(mFallbackSearchView, showAllApps ? 1f : 0f, Interpolators.LINEAR);
    }

    public void requestSearch() {
        mSearchRequested = true;
    }

    public void showKeyboardOnSearchRequest() {
        if (mSearchRequested) {
            mSearchRequested = false;
            mFallbackSearchView.showKeyboard();
        }
    }

    public boolean tryClearSearch() {
        if (mFallbackSearchView.length() > 0) {
            mFallbackSearchView.setText("");
            mSearchBarController.refreshSearchResult();
            mAppsView.requestFocus();
            return true;
        }
        return false;
    }
}
