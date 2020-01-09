package amirz.shade.customization;

import com.android.launcher3.popup.SystemShortcut;
import com.android.launcher3.popup.SystemShortcutFactory;

@SuppressWarnings("unused")
public class ShortcutFactory extends SystemShortcutFactory {
    public ShortcutFactory() {
        super(new SystemShortcut.AppInfo(),
                new SystemShortcut.Widgets(),
                new SystemShortcut.Install());
    }
}
