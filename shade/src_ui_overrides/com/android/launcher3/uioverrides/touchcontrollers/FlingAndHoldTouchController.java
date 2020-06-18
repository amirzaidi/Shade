/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.android.launcher3.uioverrides.touchcontrollers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherState;
import com.android.launcher3.LauncherStateManager;
import com.android.launcher3.anim.AnimatorSetBuilder;
import com.android.quickstep.util.MotionPauseDetector;

import amirz.shade.services.GlobalActionService;
import amirz.shade.util.HapticFeedback;

import static amirz.shade.services.Services.PERM;
import static amirz.shade.services.GlobalActionService.RECENTS;
import static android.os.SystemClock.uptimeMillis;
import static com.android.launcher3.LauncherState.*;
import static com.android.launcher3.LauncherStateManager.ANIM_ALL;
import static com.android.launcher3.anim.AnimatorSetBuilder.*;
import static com.android.launcher3.anim.Interpolators.*;

/**
 * Touch controller which handles swipe and hold to go to Overview
 */
public class FlingAndHoldTouchController extends PortraitStatesTouchController {
    private static final String TAG = "FlingAndHoldTouchController";

    private static final long ATOMIC_DURATION_FROM_PAUSED_TO_OVERVIEW = 295;
    private static final long ATOMIC_DURATION_FROM_PAUSED_TO_RECENTS = 120;

    private static final float MAX_DISPLACEMENT_PERCENT = 0.25f;

    private final MotionPauseDetector mMotionPauseDetector;
    private final float mMotionPauseMinDisplacement;
    private final float mMotionPauseMaxDisplacement;
    private boolean mAnimateToRecents;
    private boolean mTrackPause;

    public FlingAndHoldTouchController(Launcher l) {
        super(l, false /* allowDragToOverview */);
        mMotionPauseDetector = new MotionPauseDetector(l);
        mMotionPauseMinDisplacement = ViewConfiguration.get(l).getScaledTouchSlop();
        mMotionPauseMaxDisplacement = getShiftRange() * MAX_DISPLACEMENT_PERCENT;
    }

    @Override
    protected long getAtomicDuration() {
        return ATOMIC_DURATION_FROM_PAUSED_TO_OVERVIEW;
    }

    @Override
    public void onDragStart(boolean start) {
        mTrackPause = true;
        mMotionPauseDetector.clear();
        super.onDragStart(start);

        mAnimateToRecents = false;
        if (start && handlingOverviewAnim()) {
            mMotionPauseDetector.setOnMotionPauseListener(isPaused -> {
                if (isPaused) {
                    Log.d(TAG, "Pause detected, opening recents");
                    mAnimateToRecents = true;
                    setBlockTouch(true);
                    simulateLiftFinger();
                }
            });
        }
    }

    private void simulateLiftFinger() {
        MotionEvent ev = MotionEvent.obtain(uptimeMillis(), uptimeMillis(),
                MotionEvent.ACTION_UP, mDetector.getDownX(), mDetector.getDownY(), 0);
        mDetector.onTouchEvent(ev);
        ev.recycle();
    }

    /**
     * @return Whether we are handling the overview animation, rather than
     * having it as part of the existing animation to the target state.
     */
    private boolean handlingOverviewAnim() {
        return mStartState == NORMAL && GlobalActionService.isRunning();
    }

    @Override
    protected AnimatorSetBuilder getAnimatorSetBuilderForStates(LauncherState fromState,
            LauncherState toState) {
        if (fromState == NORMAL && toState == ALL_APPS) {
            AnimatorSetBuilder builder = new AnimatorSetBuilder();
            // Get workspace out of the way quickly, to prepare for potential pause.
            builder.setInterpolator(ANIM_WORKSPACE_SCALE, DEACCEL_3);
            builder.setInterpolator(ANIM_WORKSPACE_TRANSLATE, DEACCEL_3);
            builder.setInterpolator(ANIM_WORKSPACE_FADE, DEACCEL_3);
            return builder;
        }
        return super.getAnimatorSetBuilderForStates(fromState, toState);
    }

    @Override
    public boolean onDrag(float displacement, MotionEvent event) {
        float upDisplacement = -displacement;
        if (upDisplacement > mMotionPauseMaxDisplacement) {
            mTrackPause = false;
            mMotionPauseDetector.clear();
        } else if (mTrackPause) {
            mMotionPauseDetector.setDisallowPause(upDisplacement < mMotionPauseMinDisplacement);
            mMotionPauseDetector.addPosition(displacement, event.getEventTime());
        }
        return super.onDrag(displacement, event);
    }

    @Override
    public void onDragEnd(float velocity) {
        if (mAnimateToRecents) {
            Log.d(TAG, "Starting recents launch animation");
            AnimatorSetBuilder builder = new AnimatorSetBuilder();
            builder.setInterpolator(ANIM_VERTICAL_PROGRESS, DEACCEL_3);
            builder.setInterpolator(ANIM_ALL_APPS_FADE, DEACCEL_3);
            LauncherStateManager stateManager = mLauncher.getStateManager();
            AnimatorSet overviewAnim = stateManager.createAtomicAnimation(
                    stateManager.getCurrentStableState(), OVERVIEW, builder,
                    ANIM_ALL, ATOMIC_DURATION_FROM_PAUSED_TO_OVERVIEW);
            overviewAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLauncher.getStateManager().goToState(NORMAL);
                    setBlockTouch(false);
                }
            });
            overviewAnim.start();
            HapticFeedback.vibrate(mLauncher);
            new Handler().postDelayed(() -> mLauncher.sendBroadcast(new Intent(RECENTS), PERM),
                    ATOMIC_DURATION_FROM_PAUSED_TO_RECENTS);
        } else {
            super.onDragEnd(velocity);
        }
        mMotionPauseDetector.clear();
    }

    @Override
    protected void updateAnimatorBuilderOnReinit(AnimatorSetBuilder builder) {
        if (handlingOverviewAnim()) {
            // We don't want the state transition to all apps to animate overview,
            // as that will cause a jump after our atomic animation.
            builder.addFlag(AnimatorSetBuilder.FLAG_DONT_ANIMATE_OVERVIEW);
        }
    }
}
