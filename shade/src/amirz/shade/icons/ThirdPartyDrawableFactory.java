package amirz.shade.icons;

import android.content.Context;

import com.android.launcher3.graphics.DrawableFactory;

import amirz.shade.icons.pack.IconPackManager;

public class ThirdPartyDrawableFactory extends DrawableFactory {
    private final Context mContext;
    private final IconPackManager mManager;

    public ThirdPartyDrawableFactory(Context context) {
        mContext = context;
        mManager = IconPackManager.get(context);
    }
}
