package amirz.shade.hidden;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.android.launcher3.R;

import amirz.shade.ShadeFont;
import amirz.shade.customization.ShadeStyle;

public class HiddenAppsActivity extends AppCompatActivity {
    private static final String TAG = "HiddenAppsActivity";

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        ShadeStyle.override(this);
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_app, R.anim.exit_launcher);
        setContentView(R.layout.activity_hidden_apps);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.MATCH_PARENT,
                Gravity.CENTER);

        View barView = getLayoutInflater().inflate(R.layout.hidden_apps_action_bar, null);
        bar.setCustomView(barView, lp);
        bar.setDisplayHomeAsUpEnabled(true);

        BiometricManager biometricManager = BiometricManager.from(this);
        int authStatus = biometricManager.canAuthenticate();
        switch (authStatus) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "App can authenticate using biometrics.");
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.d(TAG, "No biometric features available on this device.");
                return;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.d(TAG, "Biometric features are currently unavailable.");
                return;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.d(TAG, "The user hasn't associated " +
                        "any biometric credentials with their account.");
                break;
        }

        if (authStatus == BiometricManager.BIOMETRIC_SUCCESS) {
            startAuthentication();
        } else {
            onUnlocked();
        }
    }

    private void startAuthentication() {
        biometricPrompt = new BiometricPrompt(this, ContextCompat.getMainExecutor(this),
                new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.d(TAG, "Authentication error: " + errString);
                finish();
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.d(TAG, "Authentication succeeded!");
                onUnlocked();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.d(TAG, "Authentication failed");
                finish();
            }
        });

        CharSequence title = getString(R.string.hidden_apps_biometric_title);
        CharSequence subtitle = getString(R.string.hidden_apps_biometric_subtitle);
        CharSequence cancel = getString(android.R.string.cancel);

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(cancel)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void onUnlocked() {
        // Fill up hidden apps list.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            overridePendingTransition(R.anim.enter_launcher, R.anim.exit_app);
        }
    }
}
