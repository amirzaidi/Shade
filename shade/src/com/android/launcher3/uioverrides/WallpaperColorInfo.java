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

import android.content.Context;

import com.android.launcher3.Utilities;

import java.util.ArrayList;

public abstract class WallpaperColorInfo {
    private static final Object sInstanceLock = new Object();
    private static WallpaperColorInfo sInstance;

    public static WallpaperColorInfo getInstance(Context context) {
        synchronized (sInstanceLock) {
            if (sInstance == null) {
                Context ctx = context.getApplicationContext();
                if (Utilities.ATLEAST_P) {
                    try {
                        sInstance = new WallpaperColorInfoVP(ctx);
                    } catch (NoSuchMethodError e) {
                        e.printStackTrace();
                    }
                }
                if (sInstance == null) {
                    sInstance = new WallpaperColorInfoVL(ctx);
                }
            }
            return sInstance;
        }
    }

    private final ArrayList<OnChangeListener> mListeners = new ArrayList<>();
    int mMainColor;
    int mSecondaryColor;
    boolean mIsDark;
    boolean mSupportsDarkText;

    private WallpaperColorInfo.OnChangeListener[] mTempListeners;

    public int getMainColor() {
        return mMainColor;
    }

    public int getSecondaryColor() {
        return mSecondaryColor;
    }

    public boolean isDark() {
        return mIsDark;
    }

    public boolean supportsDarkText() {
        return mSupportsDarkText;
    }

    public void addOnChangeListener(OnChangeListener listener) {
        mListeners.add(listener);
    }

    public void removeOnChangeListener(OnChangeListener listener) {
        mListeners.remove(listener);
    }

    public void notifyChange() {
        WallpaperColorInfo.OnChangeListener[] copy =
                mTempListeners != null && mTempListeners.length == mListeners.size() ?
                        mTempListeners : new WallpaperColorInfo.OnChangeListener[mListeners.size()];

        // Create a new array to avoid concurrent modification when the activity destroys itself.
        mTempListeners = mListeners.toArray(copy);
        for (WallpaperColorInfo.OnChangeListener listener : mTempListeners) {
            listener.onExtractedColorsChanged(this);
        }
    }

    public interface OnChangeListener {
        void onExtractedColorsChanged(WallpaperColorInfo wallpaperColorInfo);
    }
}