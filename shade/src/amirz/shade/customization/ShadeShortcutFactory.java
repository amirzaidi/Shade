package amirz.shade.customization;

import android.app.ActivityOptions;
import android.graphics.Rect;
import android.view.View;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.R;
import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.popup.SystemShortcutFactory;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.util.PackageManagerHelper;

@SuppressWarnings("unused")
public class ShadeShortcutFactory extends SystemShortcutFactory {
    public ShadeShortcutFactory() {
        super(new BottomSheetShortcut(),
                new SystemShortcut.Widgets(),
                new SystemShortcut.Install());
    }

    public static class BottomSheetShortcut extends SystemShortcut {
        public BottomSheetShortcut() {
            super(R.drawable.ic_info_no_shadow, R.string.app_info_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {
            return (view) -> {
                dismissTaskMenuView(activity);
            Rect sourceBounds = activity.getViewBounds(view);
            new PackageManagerHelper(activity).startDetailsActivityForInfo(
                    itemInfo, sourceBounds, ActivityOptions.makeBasic().toBundle());
            activity.getUserEventDispatcher().logActionOnControl(LauncherLogProto.Action.Touch.TAP,
                    LauncherLogProto.ControlType.APPINFO_TARGET, view);
            };
        }
    }
}
