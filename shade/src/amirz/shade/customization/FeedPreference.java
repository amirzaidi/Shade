package amirz.shade.customization;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import java.util.Arrays;
import java.util.List;

import amirz.shade.ShadeSettings;
import amirz.shade.feed.FeedProviders;

public class FeedPreference extends AutoUpdateListPreference {
    public FeedPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FeedPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FeedPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FeedPreference(Context context) {
        super(context);
    }

    @Override
    protected void load() {
        Context context = getContext();
        List<ApplicationInfo> aiList = FeedProviders.query(context);

        CharSequence[] keys = new String[aiList.size() + 1];
        CharSequence[] values = new String[keys.length];
        String defaultValue = "";
        int i = 0;

        // First value, disabled
        keys[i] = context.getString(R.string.pref_feed_provider_none);
        values[i++] = "";

        PackageManager pm = context.getPackageManager();

        // List of available feeds
        for (ApplicationInfo ai : aiList) {
            keys[i] = ai.loadLabel(pm);
            values[i] = ai.packageName;
            if (values[i].equals(FeedProviders.DEFAULT)) {
                defaultValue = FeedProviders.DEFAULT;
            }
            try {
                PackageInfo pi = pm.getPackageInfo(ai.packageName, 0);
                keys[i] = keys[i] + " " + pi.versionName;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            i++;
        }

        setEntries(keys);
        setEntryValues(values);

        setDefaultValue(defaultValue);
        String v = getValue();
        if (!TextUtils.isEmpty(v) && !Arrays.asList(values).contains(v)) {
            setValue(defaultValue);
        }
    }

    public static String get(Context context) {
        return Utilities.getPrefs(context).getString(ShadeSettings.PREF_FEED_PROVIDER,
                FeedProviders.DEFAULT);
    }
}
