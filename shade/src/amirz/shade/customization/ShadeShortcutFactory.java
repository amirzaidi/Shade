package amirz.shade.customization;

import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.popup.SystemShortcutFactory;

@SuppressWarnings("unused")
public class ShadeShortcutFactory extends SystemShortcutFactory {
    public ShadeShortcutFactory() {
        super(new SystemShortcut.AppInfo(),
                new SystemShortcut.Widgets(),
                new SystemShortcut.Install());
    }
}
