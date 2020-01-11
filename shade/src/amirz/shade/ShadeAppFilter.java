package amirz.shade;

import android.content.ComponentName;
import android.content.Context;
import android.os.UserHandle;

import com.android.launcher3.AppFilter;
import com.android.launcher3.BuildConfig;

import java.util.HashSet;
import java.util.Set;

import amirz.shade.hidden.HiddenAppsDatabase;

@SuppressWarnings("unused")
public class ShadeAppFilter extends AppFilter {
    private final Context mContext;
    private final Set<ComponentName> mFilter = new HashSet<>();

    public ShadeAppFilter(Context context) {
        mContext = context;
        mFilter.add(new ComponentName(BuildConfig.APPLICATION_ID, ShadeLauncher.class.getName()));
    }

    @Override
    public boolean shouldShowApp(ComponentName app, UserHandle user) {
        return !mFilter.contains(app) && !HiddenAppsDatabase.isHidden(mContext, app, user);
    }
}
