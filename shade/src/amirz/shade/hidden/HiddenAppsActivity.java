package amirz.shade.hidden;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.launcher3.R;

import amirz.shade.ShadeFont;
import amirz.shade.customization.ShadeStyle;

public class HiddenAppsActivity extends AppCompatActivity {
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
