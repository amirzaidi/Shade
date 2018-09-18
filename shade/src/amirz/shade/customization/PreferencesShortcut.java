package amirz.shade.customization;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.popup.SystemShortcut;

public class PreferencesShortcut extends SystemShortcut.AppInfo {
    public PreferencesShortcut(Context context) {
        super(R.drawable.ic_setting, R.string.app_preferences);
    }

    @Override
    public View.OnClickListener getOnClickListener(BaseDraggingActivity launcher,
                                                   ItemInfo itemInfo) {
        final View.OnClickListener appInfoListener = super.getOnClickListener(launcher, itemInfo);
        return new View.OnClickListener() {
            private PreferencesBottomSheet cbs;

            @Override
            public void onClick(View v) {
                if (cbs == null) {
                    AbstractFloatingView.closeAllOpenViews(launcher);
                    cbs = (PreferencesBottomSheet) launcher.getLayoutInflater().inflate(
                                    R.layout.app_edit_bottom_sheet,
                                    launcher.getDragLayer(),
                                    false);
                    cbs.setOnAppInfoClick(appInfoListener);
                    cbs.populateAndShow(itemInfo);
                }
            }
        };
    }
}
