package amirz.shade.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.util.Themes;
import com.android.searchlauncher.SmartspaceHostView;

import amirz.shade.ShadeFont;

public class ThemedSmartspaceHostView extends SmartspaceHostView {
    public ThemedSmartspaceHostView(Context context) {
        super(context);
    }

    @Override
    public void updateAppWidget(RemoteViews remoteViews) {
        super.updateAppWidget(remoteViews);
        overrideView();
    }

    private void overrideView() {
        Context context = getContext();
        Typeface tf = ShadeFont.getTypeface(context);

        int textColor = Themes.getAttrColor(context, R.attr.workspaceTextColor);
        int shadowColor = Themes.getAttrColor(context, R.attr.smartspaceTextShadowColor);
        TypedValue outValue = new TypedValue();
        getResources().getValue(R.dimen.smartspaceLetterSpacing, outValue, true);
        float letterSpacing = outValue.getFloat();
        int dividerSize = context.getResources().getDimensionPixelSize(R.dimen.smartspaceDivider) + 1;

        overrideView(tf, this, textColor, shadowColor, letterSpacing, dividerSize);
    }

    private static void overrideView(Typeface tf, View v, int textColor, int shadowColor,
                                     float letterSpacing, int maxDividerSize) {
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            if (tf != null) {
                tv.setTypeface(tf);
            }
            tv.setTextColor(textColor);
            tv.setShadowLayer(tv.getShadowRadius(), tv.getShadowDx(), tv.getShadowDy(), shadowColor);
            tv.setLetterSpacing(letterSpacing);
        } else if (v instanceof ImageView) {
            ImageView iv = (ImageView) v;
            ViewGroup.LayoutParams lp = iv.getLayoutParams();
            if (lp.height <= maxDividerSize || lp.width <= maxDividerSize) {
                iv.setBackgroundColor(textColor);
            }
        } else if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                overrideView(tf, vg.getChildAt(i), textColor, shadowColor, letterSpacing, maxDividerSize);
            }
        }
    }
}
