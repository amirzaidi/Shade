package amirz.shade.allapps;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EdgeEffect;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.launcher3.R;
import com.android.launcher3.allapps.AllAppsContainerView;

import amirz.shade.search.EditText;

public class AllAppsView extends AllAppsContainerView {
    private static final float OPEN_HIDDEN_APPS_THRES = 0.5f;
    private static final int OPEN_HIDDEN_APPS_MS = 400;

    private final AllAppsSpring mController;

    private final Handler mHandler = new Handler();
    private boolean mQueuedOpenHiddenApps;
    private final Runnable mOpenHiddenApps = () -> {
        dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(),
                MotionEvent.ACTION_UP,
                0, 0, 0));

        EditText v = findViewById(R.id.fallback_search_view);
        v.setText(R.string.search_hidden);
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
        float maxShift = getSearchView().getHeight() / 2f;
        if (shift < 0f) {
            maxShift *= -1f;
        }
        float fact = shift / maxShift;
        super.setDampedScrollShift(fact / (fact + 1f) * maxShift);
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
    }
}