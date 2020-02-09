package amirz.aidlbridge;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

@SuppressWarnings("unused")
public class LauncherClientBridge extends IBridgeCallback.Stub implements ServiceConnection {
    private static final String INTERFACE_DESCRIPTOR = "amirz.aidlbridge.IBridge";

    private final ServiceConnection mClientService;
    private final int mFlags;
    private ComponentName mConnectionName;

    public LauncherClientBridge(ServiceConnection clientService, int flags) {
        mClientService = clientService;
        mFlags = flags;
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        try {
            if (INTERFACE_DESCRIPTOR.equals(service.getInterfaceDescriptor())) {
                IBridge.Stub.asInterface(service).bindService(this, mFlags);
            } else {
                mConnectionName = name;
                mClientService.onServiceConnected(name, service);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (mConnectionName != null) {
            mClientService.onServiceDisconnected(mConnectionName);
            mConnectionName = null;
        }
    }
}
