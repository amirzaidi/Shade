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

@TargetApi(28)
public class RecentsIconLoader extends NormalizedIconLoader {

    public RecentsIconLoader(Context context, TaskKeyLruCache<Drawable> iconCache,
                             LruCache<ComponentName, ActivityInfo> activityInfoCache) {
        super(context, iconCache, activityInfoCache);
    }

    @Override
    public Drawable getIcon(Task t) {
        ComponentKey key = new ComponentKey(t.key.sourceComponent,
                UserHandle.getUserHandleForUid(t.key.userId));

        Drawable icon = ThirdPartyIconProvider.getByKey(mContext, key, 0);
        return icon == null
                ? super.getIcon(t)
                : createBadgedDrawable(icon, t.key.userId, t.taskDescription);
    }
}
