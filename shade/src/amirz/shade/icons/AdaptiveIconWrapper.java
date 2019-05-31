package amirz.shade.icons;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.FixedScaleDrawable;
import com.android.launcher3.graphics.IconNormalizer;
import com.android.launcher3.graphics.LauncherIcons;

public class AdaptiveIconWrapper {
    private static AdaptiveIconWrapper sInstance;

    public static synchronized AdaptiveIconWrapper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new AdaptiveIconWrapper(context);
        }
        return sInstance;
    }

    private final Context mContext;
    private final AdaptiveIconDrawable mWrapper;

    private AdaptiveIconWrapper(Context context) {
        mContext = context;
        mWrapper = Utilities.ATLEAST_OREO
                ? (AdaptiveIconDrawable) context.getDrawable(
                        R.drawable.adaptive_icon_drawable_wrapper).mutate()
                : null;
    }

    @TargetApi(26)
    public Drawable wrap(Drawable icon) {
        if (Utilities.ATLEAST_OREO && !(icon instanceof AdaptiveIconDrawable)) {
            boolean[] outShape = new boolean[1];
            mWrapper.setBounds(0, 0, 1, 1);

            LauncherIcons icons = LauncherIcons.obtain(mContext);
            IconNormalizer normalizer = icons.getNormalizer();
            float scale = normalizer.getScale(icon, null, mWrapper.getIconMask(), outShape);
            icons.recycle();

            if (!outShape[0]) {
                FixedScaleDrawable fsd = ((FixedScaleDrawable) mWrapper.getForeground());
                fsd.setDrawable(icon);
                fsd.setScale(scale);
                ((ColorDrawable) mWrapper.getBackground()).setColor(Color.WHITE);
                return mWrapper;
            }
        }
        return icon;
    }
}
