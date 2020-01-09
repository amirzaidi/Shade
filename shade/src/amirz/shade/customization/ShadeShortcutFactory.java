package amirz.shade.customization;

import android.content.Context;
import android.view.View;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.popup.SystemShortcutFactory;

@SuppressWarnings("unused")
public class ShadeShortcutFactory extends SystemShortcutFactory {
    public ShadeShortcutFactory() {
        super(new BottomSheetShortcut(),
                new SystemShortcut.Widgets(),
                new SystemShortcut.Install());
    }

    public static class BottomSheetShortcut extends SystemShortcut.AppInfo {
        @Override
        public View.OnClickListener getOnClickListener(
                BaseDraggingActivity activity, ItemInfo itemInfo) {
            Launcher launcher = (Launcher) activity;
            final View.OnClickListener aiListener = super.getOnClickListener(launcher, itemInfo);
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
                        cbs.setOnAppInfoClick(aiListener);
                        cbs.populateAndShow(itemInfo);
                    }
                }
            };
        }
    }
}
