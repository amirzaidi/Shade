package amirz.shade.views;

import android.content.Context;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.util.Themes;
import com.android.searchlauncher.SmartspaceHostView;

import amirz.shade.ShadeFont;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

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

        if (getChildCount() == 1) {
            View topLevel = getChildAt(0);
            if (topLevel instanceof RelativeLayout) {
                RelativeLayout rl = (RelativeLayout) topLevel;
                if (rl.getChildCount() == 1) {
                    View internal = rl.getChildAt(0);
                    if (internal instanceof LinearLayout) {
                        LinearLayout internall = (LinearLayout) internal;
                        if (internall.getChildCount() == 2
                                && internall.getOrientation() == LinearLayout.VERTICAL) {
                            overrideLayout(internall);
                        }
                    }
                }
            }
        }

        overrideView(tf, this, textColor, shadowColor, letterSpacing, dividerSize);
    }

    private void overrideLayout(LinearLayout l) {
        ViewGroup.LayoutParams llp = l.getLayoutParams();
        llp.height = MATCH_PARENT;
        l.setLayoutParams(llp);
        l.setClipChildren(false);

        View topView = l.getChildAt(0);
        View bottomView = l.getChildAt(1);
        if (topView instanceof LinearLayout && bottomView instanceof LinearLayout) {
            LinearLayout topl = (LinearLayout) topView;
            LinearLayout bottoml = (LinearLayout) bottomView;

            LinearLayout.LayoutParams lp =
                    new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            lp.gravity = Gravity.BOTTOM;
            for (int i = 0; i < topl.getChildCount(); i++) {
                topl.getChildAt(i).setLayoutParams(lp);
            }

            if (Themes.getAttrBoolean(getContext(), R.attr.isWorkspaceDarkText)
                    && bottoml.getChildCount() > 0) {
                View v = bottoml.getChildAt(0);
                if (v instanceof ImageView) {
                    v.setVisibility(View.GONE);
                }
            }

            LinearLayout.LayoutParams toplp
                    = (LinearLayout.LayoutParams) topl.getLayoutParams();
            toplp.weight = 1f;
            toplp.setMargins(0, 0, 0, 0);
            topl.setLayoutParams(toplp);

            LinearLayout.LayoutParams bottomlp
                    = (LinearLayout.LayoutParams) bottoml.getLayoutParams();
            bottomlp.weight = 1f;
            bottomlp.setMargins(0, 0, 0, 0);
            bottoml.setLayoutParams(bottomlp);
        }
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
