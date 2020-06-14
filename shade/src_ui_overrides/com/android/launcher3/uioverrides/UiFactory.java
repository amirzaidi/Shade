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

package com.android.launcher3.uioverrides;

import android.app.Activity;
import android.app.Person;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ShortcutInfo;
import android.os.Bundle;
import android.os.CancellationSignal;

import com.android.launcher3.AbstractFloatingView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.LauncherState.ScaleAndTranslation;
import com.android.launcher3.LauncherStateManager.StateHandler;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.graphics.RotationMode;

import java.io.PrintWriter;

import amirz.shade.hidden.HiddenAppsDrawerState;

import static com.android.launcher3.AbstractFloatingView.TYPE_ALL;
import static com.android.launcher3.AbstractFloatingView.TYPE_HIDE_BACK_BUTTON;
import static com.android.launcher3.allapps.DiscoveryBounce.HOME_BOUNCE_SEEN;
import static com.android.launcher3.allapps.DiscoveryBounce.SHELF_BOUNCE_SEEN;

public class UiFactory extends RecentsUiFactory {
    public static Runnable enableLiveUIChanges(Launcher l) {
        return null;
    }

    public static StateHandler[] getStateHandler(Launcher launcher) {
        return new StateHandler[] {
                launcher.getAllAppsController(),
                launcher.getWorkspace(),
                launcher.findViewById(R.id.scrim_view)
        };
    }

    public static void resetOverview(Launcher launcher) { }

    public static void onLauncherStateOrFocusChanged(Launcher launcher) {
        updateDisallowBackGesture(launcher);
    }

    public static void onCreate(Launcher launcher) {
        launcher.getSharedPrefs().edit()
                .putBoolean(HOME_BOUNCE_SEEN, true)
                .putBoolean(SHELF_BOUNCE_SEEN, true)
                .apply();
    }

    public static void onStart(Launcher launcher) { }

    public static void onEnterAnimationComplete(Context context) {}

    public static void onLauncherStateOrResumeChanged(Launcher launcher) {
        if (launcher.getStateManager().getState() == LauncherState.NORMAL
                && launcher.getAllAppsController().getProgress() == 1f) {
            HiddenAppsDrawerState.getInstance(launcher).setRevealed(false);
        }
        updateDisallowBackGesture(launcher);
    }

    private static void updateDisallowBackGesture(Launcher launcher) {
        boolean shouldBackButtonBeHidden = launcher != null
                && launcher.getStateManager().getState().hideBackButton
                && launcher.hasWindowFocus();
        if (shouldBackButtonBeHidden) {
            // Show the back button if there is a floating view visible.
            shouldBackButtonBeHidden = AbstractFloatingView.getTopOpenViewWithType(launcher,
                    TYPE_ALL & ~TYPE_HIDE_BACK_BUTTON) == null;
        }
        if (launcher != null && launcher.getDragLayer() != null) {
            launcher.getRootView().setDisallowBackGesture(shouldBackButtonBeHidden);
        }
    }

    public static void onTrimMemory(Launcher launcher, int level) { }

    public static void useFadeOutAnimationForLauncherStart(Launcher launcher,
            CancellationSignal cancellationSignal) { }

    public static boolean dumpActivity(Activity activity, PrintWriter writer) {
        return false;
    }

    public static void setBackButtonAlpha(Launcher launcher, float alpha, boolean animate) { }

    public static ScaleAndTranslation getOverviewScaleAndTranslationForNormalState(Launcher l) {
        return new ScaleAndTranslation(1.1f, 0f, 0f);
    }

    public static RotationMode getRotationMode(DeviceProfile dp) {
        return RotationMode.NORMAL;
    }

    public static boolean startIntentSenderForResult(Activity activity, IntentSender intent,
            int requestCode, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags,
            Bundle options) {
        return false;
    }

    public static boolean startActivityForResult(Activity activity, Intent intent, int requestCode,
            Bundle options) {
        return false;
    }

    public static void resetPendingActivityResults(Launcher launcher, int requestCode) { }

    public static void clearSwipeSharedState(boolean finishAnimation) {}

    public static Person[] getPersons(ShortcutInfo si) {
        return Utilities.EMPTY_PERSON_ARRAY;
    }
}
