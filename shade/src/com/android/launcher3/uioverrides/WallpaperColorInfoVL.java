/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3.uioverrides;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Pair;

import com.android.launcher3.Utilities;
import com.android.launcher3.uioverrides.dynamicui.ColorExtractionAlgorithm;
import com.android.launcher3.uioverrides.dynamicui.WallpaperColorsCompat;
import com.android.launcher3.uioverrides.dynamicui.WallpaperManagerCompat;

import static android.app.WallpaperManager.FLAG_SYSTEM;

@TargetApi(Build.VERSION_CODES.N)
public class WallpaperColorInfoVL extends WallpaperColorInfo
        implements WallpaperManagerCompat.OnColorsChangedListenerCompat {

    private static final int FALLBACK_COLOR = Color.WHITE;

    private final ColorExtractionAlgorithm mExtractionType;

    WallpaperColorInfoVL(Context context) {
        mExtractionType = ColorExtractionAlgorithm.newInstance(context);

        WallpaperManagerCompat wm = WallpaperManagerCompat.getInstance(context);
        wm.addOnColorsChangedListener(this);
        update(wm.getWallpaperColors(FLAG_SYSTEM));
    }

    @Override
    public void onColorsChanged(WallpaperColorsCompat colors, int which) {
        if ((which & FLAG_SYSTEM) != 0) {
            update(colors);
            notifyChange();
        }
    }

    private void update(WallpaperColorsCompat wallpaperColors) {
        Pair<Integer, Integer> colors = mExtractionType.extractInto(wallpaperColors);
        if (colors != null) {
            mMainColor = colors.first;
            mSecondaryColor = colors.second;
        } else {
            mMainColor = FALLBACK_COLOR;
            mSecondaryColor = FALLBACK_COLOR;
        }

        mSupportsDarkText = wallpaperColors != null && (wallpaperColors.getColorHints()
                & WallpaperColorsCompat.HINT_SUPPORTS_DARK_TEXT) > 0;

        mIsDark = wallpaperColors != null && (wallpaperColors.getColorHints()
                & WallpaperColorsCompat.HINT_SUPPORTS_DARK_THEME) > 0;
    }
}