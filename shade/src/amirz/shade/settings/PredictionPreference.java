package amirz.shade.settings;

import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.AttributeSet;

import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import amirz.shade.ShadeSettings;

public class PredictionPreference extends SwitchPreference implements ShadeSettings.OnResumePreferenceCallback {
    public static final String KEY = "pref_predictions";

    public PredictionPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        load();
    }

    public PredictionPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        load();
    }

    public PredictionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        load();
    }

    public PredictionPreference(Context context) {
        super(context);
        load();
    }

    private void load() {
        setOnPreferenceChangeListener((preference, newValue) -> {
            boolean accessGranted = isAccessGranted();
            setSummary(accessGranted && (boolean) newValue);
            return accessGranted;
        });
        reload();
    }

    private void reload() {
        final Context context = getContext();
        boolean accessGranted = isAccessGranted();

        setSummary(accessGranted && isEnabled(context));
        setFragment(accessGranted ? null : UsageAccessConfirmation.class.getName());
    }

    @Override
    public void onResume() {
        reload();
    }

    private void setSummary(boolean isEnabled) {
        setChecked(isEnabled);
        setSummary(isEnabled
                    ? R.string.predictions_on
                    : R.string.predictions_off);
    }

    private boolean isAccessGranted() {
        try {
            Context context = getContext();
            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(BuildConfig.APPLICATION_ID, 0);
            AppOpsManager appOpsManager = context.getSystemService(AppOpsManager.class);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    applicationInfo.uid, applicationInfo.packageName);
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private static void openSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static class UsageAccessConfirmation
            extends DialogFragment implements DialogInterface.OnClickListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            String msg = context.getString(R.string.msg_missing_usage_access,
                    context.getString(R.string.derived_app_name));
            return new AlertDialog.Builder(context)
                    .setTitle(R.string.title_app_suggestions)
                    .setMessage(msg)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(R.string.title_change_settings, this)
                    .create();
        }

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            Context context = getActivity();
            setEnabled(context, true);
            openSetting(context);
        }
    }

    public static void setEnabled(Context context, boolean enabled) {
        Utilities.getPrefs(context).edit().putBoolean(KEY, enabled).apply();
    }

    public static boolean isEnabled(Context context) {
        return Utilities.getPrefs(context).getBoolean(KEY, true);
    }
}
