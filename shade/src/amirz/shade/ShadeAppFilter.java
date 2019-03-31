package amirz.shade;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

import com.android.launcher3.AppFilter;
import com.android.launcher3.BuildConfig;
import com.android.launcher3.util.ComponentKey;

import amirz.shade.customization.CustomizationDatabase;

public class ShadeAppFilter extends AppFilter {
    private final Context mContext;

    public ShadeAppFilter(Context context) {
        mContext = context;
    }

    @Override
    public boolean shouldShowApp(ComponentName componentName, UserHandle user) {
        if (CustomizationDatabase.isHidden(mContext,
                new ComponentKey(componentName, user))) {
            return false;
        }

        return !BuildConfig.APPLICATION_ID.equals(componentName.getPackageName());
    }
}
