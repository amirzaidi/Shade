/*
 * Copyright (C) 2015 The Android Open Source Project
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

package com.android.launcher3.config;

import com.android.launcher3.uioverrides.TogglableFlag;

/**
 * Defines a set of flags used to control various launcher behaviors
 */
public final class FeatureFlags extends BaseFlags {
    public static final TogglableFlag DRAG_HANDLE = new TogglableFlag("DRAG_HANDLE_ACCESSIBILITY", false,
            "Show a drag handle when accessibility services are enabled.");

    // Enable moving the QSB on the 0th screen of the workspace
    public static boolean QSB_ON_FIRST_SCREEN = true;

    // When enabled add space for a search widget in the dock.
    public static boolean HOTSEAT_WIDGET = true;

    // When enabled load partner overrides from package.
    public static final boolean PARTNER_CUSTOMIZATION = false;

    // When enabled maintain the app drawer grid separately from the workspace.
    public static final boolean MAINTAIN_DRAWER_GRID = false;

    private FeatureFlags() {
        // Prevent instantiation
    }
}
