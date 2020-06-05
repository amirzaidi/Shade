package amirz.shade.allapps;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EdgeEffect;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.allapps.AllAppsContainerView;

import amirz.shade.search.AllAppsSearchBackground;
import amirz.shade.search.EditText;
import amirz.shade.util.AppReloader;

public class AllAppsView extends AllAppsContainerView {
    private static final float OPEN_HIDDEN_APPS_THRES = 0.5f;
    private static final int OPEN_HIDDEN_APPS_MS = 400;

    private final AllAppsSpring mController;

    private final Handler mHandler = new Handler();
    private boolean mQueuedOpenHiddenApps;
    private final Runnable mOpenHiddenApps = () -> {
        EditText v = findViewById(R.id.fallback_search_view_text);
        if (TextUtils.isEmpty(v.getText())
                && !AppReloader.get(getContext()).hiddenApps().isEmpty()) {
            dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                    SystemClock.uptimeMillis(),
                    MotionEvent.ACTION_UP,
                    0, 0, 0));
            v.setText(R.string.search_hidden);
            requestFocus();
        }
    };

    public AllAppsView(Context context) {
        this(context, null);
    }

    public AllAppsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AllAppsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mController = new AllAppsSpring(this);
    }

    @Override
    public void setDampedScrollShift(float shift) {
        checkShouldOpenHiddenApps(shift);
        float maxShift = getSearchView().getHeight() * 0.5f;
        float oldShift = Utilities.boundToRange(shift, -maxShift, maxShift);

        if (shift < 0f) {
            maxShift *= -1f;
        }
        float fact = shift / maxShift;
        float newShift = fact / (fact + 1f) * maxShift;

        super.setDampedScrollShift(0.3f * oldShift + 0.7f * newShift);
    }

    private void checkShouldOpenHiddenApps(float shift) {
        shift *= -1f;
        float threshold = getSearchView().getHeight() * OPEN_HIDDEN_APPS_THRES;
        if (mQueuedOpenHiddenApps) {
            if (shift < threshold) {
                mHandler.removeCallbacks(mOpenHiddenApps);
                mQueuedOpenHiddenApps = false;
            }
        } else {
            if (shift >= threshold) {
                mHandler.postDelayed(mOpenHiddenApps, OPEN_HIDDEN_APPS_MS);
                mQueuedOpenHiddenApps = true;
            }
        }
    }

    @Override
    public RecyclerView.EdgeEffectFactory createEdgeEffectFactory() {
        return new RecyclerView.EdgeEffectFactory() {
            @NonNull
            @Override
            protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction) {
                switch (direction) {
                    case DIRECTION_TOP: return mController.createSide(+1f);
                    case DIRECTION_BOTTOM: return mController.createSide(-1f);
                }
                return super.createEdgeEffect(view, direction);
            }
        };
    }

    @Override
    public void setupHeader() {
        super.setupHeader();
        getFloatingHeaderView().reset(false);

        AllAppsSearchBackground bg = findViewById(R.id.fallback_search_view);
        bg.setShadowAlpha(0);
        addElevationController(bg.getElevationController());
    }
}