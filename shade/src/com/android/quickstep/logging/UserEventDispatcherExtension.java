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
package com.android.quickstep.logging;

import android.content.Context;

import com.android.launcher3.logging.UserEventDispatcher;

/**
 * This class handles AOSP MetricsLogger function calls and logging around
 * quickstep interactions.
 */
@SuppressWarnings("unused")
public class UserEventDispatcherExtension extends UserEventDispatcher {

    public static final int ALL_APPS_PREDICTION_TIPS = 2;

    private static final String TAG = "UserEventDispatcher";

    public UserEventDispatcherExtension(Context context) { }

    public void logStateChangeAction(int action, int dir, int downX, int downY,
                                     int srcChildTargetType, int srcParentContainerType,
                                     int dstContainerType, int pageIndex) {
        // No-op
    }

    public void logActionTip(int actionType, int viewType) {
        // No-op
    }
}
