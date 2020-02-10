package amirz.aidlbridge;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import com.android.launcher3.BuildConfig;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class LauncherClientIntent {
    private static final String KEY_VERSION = "service.api.version";

    private final static String PASSTHROUGH = "com.google.android.googlequicksearchbox";
    private final static String AIDL_BRIDGE = "amirz.shade.aidlbridge";
    private final static String PIXEL_BRIDGE = "com.google.android.apps.nexuslauncher";

    private final static String DEFAULT = BuildConfig.DEBUG ? PASSTHROUGH : AIDL_BRIDGE;

    private static String sPkg;

    public static String getPackage() {
        return sPkg == null ? DEFAULT : sPkg;
    }

    public static void setPackage(String pkg) {
        sPkg = pkg;
    }

    public static String getRecommendedPackage(Context context) {
        List<ApplicationInfo> providerInfos = LauncherClientIntent.query(context);
        List<String> providers = new ArrayList<>();
        for (ApplicationInfo provider : providerInfos) {
            providers.add(provider.packageName);
        }

        // If pass-through is allowed, use it.
        if (providers.contains(PASSTHROUGH)) {
            return PASSTHROUGH;
        }

        // Check for Pixel Bridge.
        if (providers.contains(PIXEL_BRIDGE)) {
            int flags = providerInfos.get(providers.indexOf(PIXEL_BRIDGE)).flags;
            if ((flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                return PIXEL_BRIDGE;
            }
        }

        // Check for AIDL Bridge alternatively.
        if (providers.contains(AIDL_BRIDGE)) {
            return AIDL_BRIDGE;
        }

        // Return the first option available.
        return providers.isEmpty() ? null : providers.get(0);
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

    public static int getServiceVersion(PackageManager pm, Intent intent) {
        int version = 1;

        // Get Google App's version.
        version = overrideServiceVersion(pm, intent, version);
        intent.setPackage(getPackage());

        // Get overridden version from the selected package.
        version = overrideServiceVersion(pm, intent, version);

        return version;
    }

    private static int overrideServiceVersion(PackageManager pm, Intent intent, int version) {
        ResolveInfo ri = pm.resolveService(intent, PackageManager.GET_META_DATA);
        return ri != null && ri.serviceInfo.metaData != null
                ? ri.serviceInfo.metaData.getInt(KEY_VERSION, version)
                : version;
    }
}
