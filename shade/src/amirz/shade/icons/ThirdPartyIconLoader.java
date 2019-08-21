package amirz.shade.icons;

import android.annotation.TargetApi;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.LruCache;

import com.android.launcher3.util.ComponentKey;
import com.android.quickstep.NormalizedIconLoader;
import com.android.systemui.shared.recents.model.Task;
import com.android.systemui.shared.recents.model.TaskKeyLruCache;

import amirz.shade.icons.pack.IconResolver;

@TargetApi(28)
public class ThirdPartyIconLoader extends NormalizedIconLoader {

    public ThirdPartyIconLoader(Context context, TaskKeyLruCache<Drawable> iconCache,
                                LruCache<ComponentName, ActivityInfo> activityInfoCache) {
        super(context, iconCache, activityInfoCache);
    }

    @Override
    public Drawable getIcon(Task t) {
        ComponentKey key = new ComponentKey(t.key.sourceComponent,
                UserHandle.getUserHandleForUid(t.key.userId));

        IconResolver.DefaultDrawableProvider fallback = () -> super.getIcon(t);
        Drawable icon = ThirdPartyIconUtils.getByKey(mContext, key, 0, fallback);
        if (icon == null) {
            icon = AdaptiveIconWrapper.getInstance(mContext).wrap(fallback.get(),
                    t.taskDescription.getPrimaryColor());
        }
        return createBadgedDrawable(icon, t.key.userId, t.taskDescription);
    }
}
