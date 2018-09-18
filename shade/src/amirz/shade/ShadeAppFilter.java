package amirz.shade;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

import com.android.launcher3.AppFilter;
import com.android.launcher3.BuildConfig;

public class ShadeAppFilter extends AppFilter {
    private final Context mContext;

    public ShadeAppFilter(Context context) {
        mContext = context;
    }

    @Override
    public boolean shouldShowApp(ComponentName componentName, UserHandle user) {
        // Check for override here

        return !BuildConfig.APPLICATION_ID.equals(componentName.getPackageName());

        //return super.shouldShowApp(componentName, user);
    }

    public void setShouldShowApp(ComponentName componentName, UserHandle user, boolean value) {
        // Set override here

        ShadeUtilities.reloadPackage(mContext, componentName.getPackageName(), user);
    }
}
