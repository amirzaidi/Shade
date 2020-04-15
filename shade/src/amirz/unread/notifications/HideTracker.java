package amirz.unread.notifications;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.os.Process;
import android.os.UserHandle;

import com.android.launcher3.compat.LauncherAppsCompat;

import java.util.List;

import amirz.shade.hidden.HiddenAppsDatabase;

public class HideTracker {
    private final Context mContext;
    private final LauncherAppsCompat mApps;
    private final UserHandle mUser;

    public HideTracker(Context context) {
        mContext = context;
        mApps = LauncherAppsCompat.getInstance(context);
        mUser = Process.myUserHandle();
    }

    boolean allActivitiesHidden(String pkg) {
        List<LauncherActivityInfo> aiList = mApps.getActivityList(pkg, mUser);
        if (aiList.size() > 0) {
            for (LauncherActivityInfo info : aiList) {
                if (!HiddenAppsDatabase.isHidden(mContext, info.getComponentName(), mUser)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
