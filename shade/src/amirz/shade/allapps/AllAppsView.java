package amirz.shade.allapps;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.EdgeEffect;

import com.android.launcher3.allapps.AllAppsContainerView;

public class AllAppsView extends AllAppsContainerView {
    private final AllAppsSpring mController;

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
        float maxShift = getSearchView().getHeight() / 2f;
        if (shift < 0f) {
            maxShift *= -1f;
        }
        float fact = shift / maxShift;
        super.setDampedScrollShift(fact / (fact + 1f) * maxShift);
    }

    @Override
    public RecyclerView.EdgeEffectFactory createEdgeEffectFactory() {
        return new RecyclerView.EdgeEffectFactory() {
            @NonNull @Override
            protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction) {
                switch (direction) {
                    case DIRECTION_TOP: return mController.createSide(+1f);
                    case DIRECTION_BOTTOM: return mController.createSide(-1f);
                }
                return super.createEdgeEffect(view, direction);
            }
        };
    }
}
