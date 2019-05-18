package amirz.shade.shortcuts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Process;
import android.os.UserHandle;
import android.support.annotation.NonNull;

import com.android.launcher3.plugin.shortcuts.ShortcutPluginClient;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;

import java.net.URISyntaxException;

public class ShortcutInfoCompatExt extends ShortcutInfoCompat {
    private static final String EXTERNAL_PREFIX = "shortcut-external-";

    static boolean isExternal(String id) {
        return id.startsWith(EXTERNAL_PREFIX);
    }

    private final String mPackageName;
    private final ComponentName mActivity;

    private final ShortcutPluginClient.ShortcutWithIcon mShortcut;

    ShortcutInfoCompatExt(ComponentName activity, ShortcutPluginClient.ShortcutWithIcon shortcut) {
        super(null);

        mPackageName = activity.getPackageName();
        mActivity = activity;
        mShortcut = shortcut;
    }

    public Drawable getIcon(Context context, int density) {
        return new BitmapDrawable(context.getResources(), mShortcut.getIcon(density));
    }

    @Override
    public Intent makeIntent() {
        try {
            String uri = mShortcut.getBundle().getString("intent");
            String id = getId();
            return Intent.parseUri(uri, Intent.URI_INTENT_SCHEME).putExtra(EXTRA_SHORTCUT_ID, id);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getPackage() {
        return mPackageName;
    }

    @Override
    public String getId() {
        return EXTERNAL_PREFIX + mShortcut.getBundle().getString("id");
    }

    @Override
    public CharSequence getShortLabel() {
        return mShortcut.getBundle().getString("shortLabel");
    }

    @Override
    public CharSequence getLongLabel() {
        return mShortcut.getBundle().getString("longLabel");
    }

    @Override
    public ComponentName getActivity() {
        Intent intent = makeIntent();
        return intent.getComponent() == null ? mActivity : intent.getComponent();
    }

    @Override
    public UserHandle getUserHandle() {
        return Process.myUserHandle();
    }

    @Override
    public boolean isPinned() {
        return false;
    }

    @Override
    public boolean isDeclaredInManifest() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return mShortcut.getBundle().getBoolean("enabled");
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public int getRank() {
        return mShortcut.getBundle().getInt("rank");
    }

    @Override
    public CharSequence getDisabledMessage() {
        return mShortcut.getBundle().getString("disabledMessage");
    }

    @NonNull
    @Override
    public String toString() {
        return "ShortcutInfoCompatVL{" + makeIntent().toString() + "}";
    }

    @Override
    public boolean canBePinned() {
        return false;
    }
}
