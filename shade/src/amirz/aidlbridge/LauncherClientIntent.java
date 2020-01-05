package amirz.aidlbridge;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Process;

import com.android.launcher3.BuildConfig;
import com.google.android.libraries.gsa.launcherclient.BuildInfo;

@SuppressWarnings("unused")
public class LauncherClientIntent {
    private final static String PASSTHROUGH = "com.google.android.googlequicksearchbox";
    private final static String BRIDGE = "amirz.aidlbridge";
    public final static String DEFAULT = BuildConfig.DEBUG ? PASSTHROUGH : BRIDGE;

    public static void setPackage(String pkg) {
        // No-op
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public static Intent getServiceIntent(Context context) {
        String pkg = context.getPackageName();
        return new Intent("com.android.launcher3.WINDOW_OVERLAY")
                .setPackage(DEFAULT)
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
