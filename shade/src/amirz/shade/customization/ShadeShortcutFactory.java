package amirz.shade.customization;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;

import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.popup.SystemShortcutFactory;
import com.android.launcher3.userevent.nano.LauncherLogProto;
import com.android.launcher3.util.PackageManagerHelper;

@SuppressWarnings("unused")
public class ShadeShortcutFactory extends SystemShortcutFactory {
    public ShadeShortcutFactory(Context context) {
        super(new BottomSheetShortcut(),
                new SystemShortcut.Widgets(),
                new SystemShortcut.Install());
    }

    private static class BottomSheetShortcut extends SystemShortcut<Launcher> {
        private BottomSheetShortcut() {
            super(R.drawable.ic_info_no_shadow, R.string.app_info_drop_target_label);
        }

        @Override
        public View.OnClickListener getOnClickListener(Launcher launcher, ItemInfo itemInfo) {
            final View.OnClickListener onClickMore = v -> onClickMore(launcher, itemInfo, v);
            return new View.OnClickListener() {
                private InfoBottomSheet cbs;

                @Override
                public void onClick(View v) {
                    if (cbs == null) {
                        dismissTaskMenuView(launcher);
                        cbs = (InfoBottomSheet) launcher.getLayoutInflater().inflate(
                                R.layout.app_info_bottom_sheet,
                                launcher.getDragLayer(),
                                false);
                        cbs.setOnAppInfoClick(onClickMore);
                        cbs.populateAndShow(itemInfo);
                    }
                }
            };
        }

        private void onClickMore(Launcher launcher, ItemInfo itemInfo, View view) {
            dismissTaskMenuView(launcher);
            Rect sourceBounds = launcher.getViewBounds(view);
            Bundle opts = launcher.getAppTransitionManager()
                    .getActivityLaunchOptions(launcher, view).toBundle();
            new PackageManagerHelper(launcher).startDetailsActivityForInfo(
                    itemInfo, sourceBounds, opts);
            launcher.getUserEventDispatcher().logActionOnControl(LauncherLogProto.Action.Touch.TAP,
                    LauncherLogProto.ControlType.APPINFO_TARGET, view);
        }
    }
}
