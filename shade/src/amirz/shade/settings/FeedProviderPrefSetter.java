package amirz.shade.settings;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import androidx.preference.ListPreference;

import com.android.launcher3.R;

import java.util.Arrays;
import java.util.List;

import amirz.aidlbridge.LauncherClientIntent;

public class FeedProviderPrefSetter implements ReloadingListPreference.OnReloadListener {
    private final Context mContext;
    private final PackageManager mPm;

    public FeedProviderPrefSetter(Context context) {
        mContext = context;
        mPm = mContext.getPackageManager();
    }

    @Override
    public void updateList(ListPreference pref) {
        List<ApplicationInfo> aiList = LauncherClientIntent.query(mContext);

        CharSequence[] keys = new String[aiList.size() + 1];
        CharSequence[] values = new String[keys.length];
        String defaultValue = LauncherClientIntent.getRecommendedPackage(mContext);
        int i = 0;

        // First value, disabled
        keys[i] = mContext.getString(R.string.pref_value_disabled);
        values[i++] = "";

        // List of available feeds
        for (ApplicationInfo ai : aiList) {
            keys[i] = ai.loadLabel(mPm);
            values[i] = ai.packageName;

            try {
                PackageInfo pi = mPm.getPackageInfo(ai.packageName, 0);
                keys[i] = mContext.getString(R.string.feed_provider_value, keys[i], pi.versionName);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
            i++;
        }

        pref.setEntries(keys);
        pref.setEntryValues(values);

        pref.setDefaultValue(defaultValue);
        String v = pref.getValue();
        if (!TextUtils.isEmpty(v) && !Arrays.asList(values).contains(v)) {
            pref.setValue(defaultValue);
        }
    }
}
