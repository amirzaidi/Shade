package amirz.shade;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.uioverrides.WallpaperColorInfo;

public class ShadeLauncher extends Launcher {
    @Override
    protected int getThemeRes(WallpaperColorInfo wallpaperColorInfo) {
        if (wallpaperColorInfo.isDark()) {
            return wallpaperColorInfo.supportsDarkText() ?
                    R.style.LauncherTheme_Dark_DarkText_Shade : R.style.LauncherTheme_Dark_Shade;
        } else {
            return wallpaperColorInfo.supportsDarkText() ?
                    R.style.LauncherTheme_DarkText_Shade : R.style.LauncherTheme_Shade;
        }
    }
}
