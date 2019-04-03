package amirz.aidlbridge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import com.android.launcher3.R;

public class PixelBridge {
    @SuppressLint("PackageManagerGetSignatures") // Warning only applies before KitKat
    public static boolean isInstalled(Context context) {
        if (!LauncherClientBridge.BRIDGE_USE) {
            return true;
        }
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(LauncherClientBridge.BRIDGE_PACKAGE,
                    PackageManager.GET_SIGNATURES);

            if (info.versionName.equals(context.getString(R.string.bridge_version)) &&
                    isSigned(context, info.signatures)) {
                return true;
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return false;
    }

    /**
     * Enforce signature check to prevent malicious edits or recompilations from being used as a bridge.
     * @param signatures Extracted signatures from the bridge package.
     * @return True if all signatures match the config, false if at least one does not match or the signatures array is empty.
     */
    private static boolean isSigned(Context context, Signature[] signatures) {
        int hash = context.getResources().getInteger(R.integer.bridge_signature_hash);
        for (Signature signature : signatures) {
            if (signature.hashCode() != hash) {
                return false;
            }
        }
        return signatures.length > 0;
    }
}
