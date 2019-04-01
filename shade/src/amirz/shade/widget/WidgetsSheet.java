/*
 * Copyright (C) 2017 The Android Open Source Project
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

package amirz.shade.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

public class WidgetsSheet extends com.android.launcher3.widget.WidgetsBottomSheet {
    public WidgetsSheet(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetsSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setInsets(Rect insets) {
        Rect insetCopy = new Rect(insets);
        if (!Utilities.ATLEAST_OREO && !mLauncher.getDeviceProfile().isVerticalBarLayout()) {
            View navBarBg = findViewById(R.id.nav_bar_bg);
            ViewGroup.LayoutParams navBarBgLp = navBarBg.getLayoutParams();
            navBarBgLp.height = insetCopy.bottom;
            navBarBg.setLayoutParams(navBarBgLp);
            insetCopy.bottom = 0;
        }
        super.setInsets(insetCopy);
    }
}
