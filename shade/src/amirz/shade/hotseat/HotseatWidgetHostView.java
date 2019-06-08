package amirz.shade.hotseat;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.qsb.QsbWidgetHostView;

class HotseatWidgetHostView extends QsbWidgetHostView {
    public HotseatWidgetHostView(Context context) {
        super(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // Remove margin and padding from children.
        for (int i = 0; i < getChildCount(); i++) {
            View view = getChildAt(i);
            view.setPadding(0, view.getPaddingTop(), 0, view.getPaddingBottom());
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            if (lp instanceof MarginLayoutParams) {
                MarginLayoutParams mlp = (MarginLayoutParams) lp;
                if (mlp.leftMargin != 0 || mlp.rightMargin != 0) {
                    mlp.leftMargin = 0;
                    mlp.rightMargin = 0;
                    view.setLayoutParams(mlp);
                }
            }
        }
    }
}
