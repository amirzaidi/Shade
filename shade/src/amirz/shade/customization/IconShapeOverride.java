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
package amirz.shade.customization;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.graphics.PathParser;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.android.launcher3.icons.AdaptiveIconCompat;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.android.launcher3.util.Executors.MODEL_EXECUTOR;

/**
 * Utility class to override shape of {@link android.graphics.drawable.AdaptiveIconDrawable}.
 */
public class IconShapeOverride {

    private static final String TAG = "IconShapeOverride";

    public static final String KEY_ICON_SHAPE = "pref_override_icon_shape";

    // Time to wait before killing the process this ensures that the progress bar is visible for
    // sufficient time so that there is no flicker.
    private static final long PROCESS_KILL_DELAY_MS = 1000;

    private static final int RESTART_REQUEST_CODE = 42; // the answer to everything

    private static SharedPreferences sPrefs;
    private static String sMaskString;

    @SuppressLint("RestrictedApi")
    public static void apply(Context context) {
        if (Utilities.ATLEAST_OREO) {
            cancelRestart(context);
            if (sPrefs == null) {
                sPrefs = Utilities.getPrefs(context);
            }
            String maskString = sPrefs.getString(KEY_ICON_SHAPE,
                    context.getString(R.string.icon_shape_override_path_circle));
            if (!Objects.equals(sMaskString, maskString)) {
                sMaskString = maskString;
                AdaptiveIconCompat.setMask(TextUtils.isEmpty(maskString)
                        ? null
                        : PathParser.createPathFromPathData(maskString));
            }
        }
    }

    static int curveTheme(Context ctx) {
        Map<String, Integer> map = new HashMap<>();
        map(ctx, map, R.string.icon_shape_override_path_square, R.style.Curvature_Square);
        map(ctx, map, R.string.icon_shape_override_path_rounded_square, R.style.Curvature_RoundedSquare);
        map(ctx, map, R.string.icon_shape_override_path_squircle, R.style.Curvature_Squircle);
        map(ctx, map, R.string.icon_shape_override_path_circle, R.style.Curvature_Circle);
        map(ctx, map, R.string.icon_shape_override_path_teardrop, R.style.Curvature_Circle);
        map(ctx, map, R.string.icon_shape_override_path_cylinder, R.style.Curvature_Circle);

        //noinspection ConstantConditions
        return map.getOrDefault(sMaskString, R.style.Curvature);
    }

    private static void map(Context context, Map<String, Integer> map, int shape, int theme) {
        map.put(context.getString(shape), theme);
    }

    public static void handlePreferenceUi(ListPreference preference) {
        preference.setOnPreferenceChangeListener(
                new PreferenceChangeHandler(preference.getContext()));
    }

    private static class PreferenceChangeHandler implements Preference.OnPreferenceChangeListener {
        private final Context mContext;

        private PreferenceChangeHandler(Context context) {
            mContext = context;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object o) {
            // Value has changed
            ProgressDialog.show(mContext,
                    null /* title */,
                    mContext.getString(R.string.icon_shape_override_progress),
                    true /* indeterminate */,
                    false /* cancelable */);
            MODEL_EXECUTOR.execute(new OverrideApplyHandler(mContext));
            return true;
        }
    }

    private static class OverrideApplyHandler implements Runnable {

        private final Context mContext;

        private OverrideApplyHandler(Context context) {
            mContext = context;
        }

        @Override
        public void run() {
            // Clear the icon cache.
            LauncherAppState.getInstance(mContext).getIconCache().clear();

            // Wait for it
            try {
                Thread.sleep(PROCESS_KILL_DELAY_MS);
            } catch (Exception e) {
                Log.e(TAG, "Error waiting", e);
            }

            // Schedule an alarm before we kill ourself.
            PendingIntent pi = getRestartIntent(mContext);
            mContext.getSystemService(AlarmManager.class).setExact(
                    AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 50, pi);

            // Kill process
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    private static PendingIntent getRestartIntent(Context context) {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_HOME)
                .setPackage(context.getPackageName())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, RESTART_REQUEST_CODE,
                homeIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_ONE_SHOT);
    }

    private static void cancelRestart(Context context) {
        context.getSystemService(AlarmManager.class).cancel(getRestartIntent(context));
    }
}
