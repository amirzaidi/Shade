package amirz.shade.customization;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public abstract class AutoUpdateListPreference extends ListPreference {
    public AutoUpdateListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        load();
    }

    public AutoUpdateListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        load();
    }

    public AutoUpdateListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        load();
    }

    public AutoUpdateListPreference(Context context) {
        super(context);
        load();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        load();
        super.onPrepareDialogBuilder(builder);
    }

    protected abstract void load();
}
