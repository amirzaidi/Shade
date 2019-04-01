package amirz.aidlbridge;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.os.Process;
import android.os.RemoteException;

import com.android.launcher3.BuildConfig;

public class LauncherClientBridge extends BridgeCallback.Stub implements ServiceConnection {
    private final static boolean BRIDGE_USE = !BuildConfig.DEBUG;
    private final static String BRIDGE_PACKAGE = "com.google.android.apps.nexuslauncher";

    public static ServiceConnection wrap(ServiceConnection clientService) {
        return BRIDGE_USE
                ? new LauncherClientBridge(clientService)
                : clientService;
    }

    public static Intent getServiceIntent(Context context) {
        String pkg = context.getPackageName();
        return new Intent("com.android.launcher3.WINDOW_OVERLAY")
                .setPackage(BRIDGE_USE ? BRIDGE_PACKAGE : "com.google.android.googlequicksearchbox")
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

    private final ServiceConnection mClientService;

    private LauncherClientBridge(ServiceConnection clientService) {
        mClientService = clientService;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Bridge bridge = Bridge.Stub.asInterface(service);
        try {
            bridge.setCallback(mClientService.getClass().hashCode(), this);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBridgeConnected(IBinder service) {
        mClientService.onServiceConnected(null, service);
    }

    @Override
    public void onBridgeDisconnected() {
        mClientService.onServiceDisconnected(null);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        onBridgeDisconnected();
    }
}
