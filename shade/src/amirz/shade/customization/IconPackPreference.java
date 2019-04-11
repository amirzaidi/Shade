package amirz.shade.customization;

import android.content.Context;
import android.util.AttributeSet;

import com.android.launcher3.R;

import java.util.Map;

import amirz.shade.icons.pack.IconPackManager;

public class IconPackPreference extends AutoUpdateListPreference {
    public IconPackPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public IconPackPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public IconPackPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public IconPackPreference(Context context) {
        super(context);
    }

    protected Map<String, CharSequence> getPacks() {
        // ToDo: filter for only ones with the right icon
        return IconPackManager.get(getContext()).getProviderNames();
    }

    @Override
    protected void load() {
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
