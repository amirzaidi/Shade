package amirz.shade.feed;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Process;

import com.android.launcher3.BuildConfig;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import amirz.shade.customization.FeedPreference;

public class FeedProviders {
    private final static String PASSTHROUGH = "com.google.android.googlequicksearchbox";
    private final static String BRIDGE = "com.google.android.apps.nexuslauncher";
    public final static String DEFAULT = BuildConfig.DEBUG ? PASSTHROUGH : BRIDGE;

    public static List<ApplicationInfo> query(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent("com.android.launcher3.WINDOW_OVERLAY")
                .setData(Uri.parse("app://" + context.getPackageName()));
        List<ApplicationInfo> aiList = new ArrayList<>();
        for (ResolveInfo ri : pm.queryIntentServices(intent, PackageManager.GET_RESOLVED_FILTER)) {
            if (ri.serviceInfo != null) {
                ApplicationInfo ai = ri.serviceInfo.applicationInfo;
                if (BuildConfig.DEBUG || !PASSTHROUGH.equals(ai.packageName)) {
                    aiList.add(ai);
                }
            }
        }
        return aiList;
    }

    public static Intent getServiceIntent(Context context) {
        String pkg = context.getPackageName();
        return new Intent("com.android.launcher3.WINDOW_OVERLAY")
                .setPackage(FeedPreference.get(context))
                .setData(Uri.parse(new StringBuilder(pkg.length() + 18)
                        .append("app://")
                        .append(pkg)
                        .append(":")
                        .append(Process.myUid())
                        .toString())
                        .buildUpon()
                        .appendQueryParameter("v", Integer.toString(7))
                        .appendQueryParameter("cv", Integer.toString(9))
                        .build());
    }
}
