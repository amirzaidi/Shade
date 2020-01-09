package amirz.shade.hidden;

import android.app.Activity;
import android.os.Bundle;

import com.android.launcher3.R;

public class HiddenAppsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.enter_app, R.anim.exit_launcher);
        setContentView(R.layout.activity_hidden_apps);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            overridePendingTransition(R.anim.enter_launcher, R.anim.exit_app);
        }
    }
}
