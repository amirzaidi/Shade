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

import static android.app.WallpaperManager.FLAG_SYSTEM;

import android.annotation.TargetApi;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.app.WallpaperManager.OnColorsChangedListener;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.android.systemui.shared.system.TonalCompat;

@TargetApi(Build.VERSION_CODES.P)
public class WallpaperColorInfoVP extends WallpaperColorInfo implements OnColorsChangedListener {
    private final TonalCompat mTonalCompat;

    WallpaperColorInfoVP(Context context) {
        mTonalCompat = new TonalCompat(context);

        WallpaperManager wm = context.getSystemService(WallpaperManager.class);
        wm.addOnColorsChangedListener(this, new Handler(Looper.getMainLooper()));
        update(wm.getWallpaperColors(FLAG_SYSTEM));
    }

    @Override
    public void onColorsChanged(WallpaperColors colors, int which) {
        if ((which & FLAG_SYSTEM) != 0) {
            update(colors);
            notifyChange();
        }
    }

    private void update(WallpaperColors wallpaperColors) {
        TonalCompat.ExtractionInfo info = mTonalCompat.extractDarkColors(wallpaperColors);
        mMainColor = info.mainColor;
        mSecondaryColor = info.secondaryColor;
        mIsDark = info.supportsDarkTheme;
        mSupportsDarkText = info.supportsDarkText;
    }
}
