package amirz.shade.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;

import com.android.launcher3.Utilities;
import com.android.launcher3.util.VibratorWrapper;

import static android.os.VibrationEffect.DEFAULT_AMPLITUDE;
import static com.android.launcher3.util.VibratorWrapper.OVERVIEW_HAPTIC;

@TargetApi(27)
public class HapticFeedback {
    private static final int FEEDBACK_LENGTH = 10;

    private static final VibrationEffect FEEDBACK =
            VibrationEffect.createOneShot(FEEDBACK_LENGTH, DEFAULT_AMPLITUDE);

    public static void vibrate(Context context) {
        if (Utilities.ATLEAST_Q) {
            VibratorWrapper.INSTANCE.get(context).vibrate(OVERVIEW_HAPTIC);
        } else {
            context.getSystemService(Vibrator.class).vibrate(FEEDBACK);
        }
    }
}
