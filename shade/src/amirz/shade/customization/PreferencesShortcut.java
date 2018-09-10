package amirz.shade.customization;

import android.content.Context;
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
    public View.OnClickListener getOnClickListener(BaseDraggingActivity activity,
                                                   ItemInfo itemInfo) {
        return view -> {
            AbstractFloatingView.closeAllOpenViews(activity);

        };
    }
}
