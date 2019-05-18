package amirz.shade.shortcuts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;

import com.android.launcher3.shortcuts.DeepShortcutManager;
import com.android.launcher3.shortcuts.ShortcutInfoCompat;
import com.android.launcher3.shortcuts.ShortcutKey;

import java.util.ArrayList;
import java.util.List;

public class ShortcutManager extends DeepShortcutManager {
    private final Context mContext;
    private final ShortcutExtension mExtension;
    private boolean mLastCallExternal;

    public ShortcutManager(Context context) {
        super();
        mContext = context;
        mExtension = new ShortcutExtension(context);
    }

    @Override
    public void unpinShortcut(final ShortcutKey key) {
        mLastCallExternal = ShortcutInfoCompatExt.isExternal(key.getId());
        if (!ShortcutInfoCompatExt.isExternal(key.getId())) {
            super.unpinShortcut(key);
        }
    }

    @Override
    public void pinShortcut(final ShortcutKey key) {
        mLastCallExternal = ShortcutInfoCompatExt.isExternal(key.getId());
        if (!mLastCallExternal) {
            super.pinShortcut(key);
        }
    }

    @Override
    public void startShortcut(String packageName, String id, Intent intent,
            Bundle startActivityOptions, UserHandle user) {
        mLastCallExternal = ShortcutInfoCompatExt.isExternal(id);
        if (mLastCallExternal) {
            mContext.startActivity(intent, startActivityOptions);
        } else {
            super.startShortcut(packageName, id, intent, startActivityOptions, user);
        }
    }

    @Override
    public Drawable getShortcutIconDrawable(ShortcutInfoCompat shortcutInfo, int density) {
        mLastCallExternal = shortcutInfo instanceof ShortcutInfoCompatExt;
        return mLastCallExternal
                ? mExtension.getShortcutIconDrawable(shortcutInfo, density)
                : super.getShortcutIconDrawable(shortcutInfo, density);
    }

    @Override
    protected List<ShortcutInfoCompat> query(int flags, String packageName,
            ComponentName activity, List<String> shortcutIds, UserHandle user) {
        List<ShortcutInfoCompat> result =
                new ArrayList<>(super.query(flags, packageName, activity, shortcutIds, user));
        result.addAll(mExtension.getForActivity(packageName, activity));
        mLastCallExternal = true;
        return result;
    }

    @Override
    public boolean wasLastCallSuccess() {
        return mLastCallExternal || super.wasLastCallSuccess();
    }

    @Override
    public List<String> overwriteIds(List<String> ids, ComponentName activity, UserHandle user) {
        ids = new ArrayList<>();
        for (ShortcutInfoCompat si : queryForShortcutsContainer(activity, null, user)) {
            ids.add(si.getId());
        }
        return ids;
    }
}
