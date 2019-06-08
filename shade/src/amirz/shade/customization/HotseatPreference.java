package amirz.shade.customization;

import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.android.launcher3.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import amirz.shade.hotseat.ShadeHotseat;

public class HotseatPreference extends AutoUpdateListPreference {
    public HotseatPreference(Context context) {
        super(context);
    }

    public HotseatPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HotseatPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public HotseatPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void load() {
        Context context = getContext();
        PackageManager pm = context.getPackageManager();
        List<AppWidgetProviderInfo> widgets = ShadeHotseat.validWidgets(context);

        CharSequence[] keys = new String[widgets.size() + 1];
        CharSequence[] values = new String[keys.length];
        int i = 0;

        // First value, system default
        keys[i] = context.getResources().getString(R.string.pref_dock_search_none);
        values[i++] = "";

        Collections.sort(widgets,
                (o1, o2) -> normalize(o1.loadLabel(pm)).compareTo(normalize(o2.loadLabel(pm))));
        for (AppWidgetProviderInfo widget : widgets) {
            keys[i] = widget.loadLabel(pm);
            values[i++] = widget.provider.flattenToShortString();
        }

        setEntries(keys);
        setEntryValues(values);

        String v = getValue();
        if (!TextUtils.isEmpty(v) && !Arrays.asList(values).contains(v)) {
            setValue("");
        }
    }

    private String normalize(String title) {
        return title.toLowerCase();
    }
}
