package amirz.shade.customization;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import com.android.launcher3.R;

import java.util.Map;

import amirz.shade.icons.pack.IconPackManager;

public class IconPackPreference extends ListPreference {
    public IconPackPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        load();
    }

    public IconPackPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        load();
    }

    public IconPackPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        load();
    }

    public IconPackPreference(Context context) {
        super(context);
        load();
    }

    protected void load() {
        reloadIconPacks();
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        reloadIconPacks();
        super.onPrepareDialogBuilder(builder);
    }

    protected Map<String, CharSequence> getPacks() {
        // ToDo: filter for only ones with the right icon
        return IconPackManager.get(getContext()).getProviderNames();
    }

    private void reloadIconPacks() {
        Context context = getContext();
        Map<String, CharSequence> packList = getPacks();

        CharSequence[] keys = new String[packList.size() + 1];
        CharSequence[] values = new String[keys.length];
        int i = 0;

        // First value, system default
        keys[i] = context.getResources().getString(R.string.icon_shape_system_default);
        values[i++] = "";

        // List of available icon packs
        for (Map.Entry<String, CharSequence> entry : packList.entrySet()) {
            keys[i] = entry.getValue();
            values[i++] = entry.getKey();
        }

        setEntries(keys);
        setEntryValues(values);
    }
}
