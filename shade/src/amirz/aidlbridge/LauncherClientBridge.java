package amirz.aidlbridge;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

public class LauncherClientBridge extends BridgeCallback.Stub implements ServiceConnection {
    private final ServiceConnection mClientService;

    public LauncherClientBridge(ServiceConnection clientService) {
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
