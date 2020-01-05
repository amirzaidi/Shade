package amirz.aidlbridge;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Process;

import com.android.launcher3.BuildConfig;
import com.google.android.libraries.gsa.launcherclient.BuildInfo;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class LauncherClientIntent {
    private final static String PASSTHROUGH = "com.google.android.googlequicksearchbox";
    private final static String BRIDGE = "amirz.aidlbridge";
    private final static String DEFAULT = BuildConfig.DEBUG ? PASSTHROUGH : BRIDGE;
    private static String sPkg;

    public static String getPackage() {
        return sPkg == null ? DEFAULT : sPkg;
    }

    public static void setPackage(String pkg) {
        sPkg = pkg;
    }

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

    @SuppressWarnings("StringBufferReplaceableByString")
    public static Intent getServiceIntent(Context context) {
        String pkg = context.getPackageName();
        return new Intent("com.android.launcher3.WINDOW_OVERLAY")
                .setPackage(getPackage())
                .setData(Uri.parse(new StringBuilder(pkg.length() + 18)
                        .append("app://")
                        .append(pkg)
                        .append(":")
                        .append(Process.myUid())
                        .toString())
                        .buildUpon()
                        .appendQueryParameter("v", Integer.toString(BuildInfo.SERVER_VERSION_CODE))
                        .appendQueryParameter("cv", Integer.toString(BuildInfo.CLIENT_VERSION_CODE))
                        .build());
    }
}
