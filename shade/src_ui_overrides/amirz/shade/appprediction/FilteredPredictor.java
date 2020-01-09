package amirz.shade.appprediction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Build;

import com.android.launcher3.BuildConfig;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import amirz.shade.settings.PredictionPreference;

import static android.content.pm.PackageManager.GET_RESOLVED_FILTER;

class FilteredPredictor extends UsageTracker {
    private static final int MAX_REMOVE_TOP = 3;
    private static final Set<String> FILTER_META;

    static {
        Set<String> filterMeta = new HashSet<>();
        filterMeta.add("android.settings.SETTINGS");
        FILTER_META = Collections.unmodifiableSet(filterMeta);
    }

    private final Context mContext;
    private final int mCount;

    FilteredPredictor(Context context, int count) {
        super(context);
        mContext = context;
        mCount = count;
    }

    List<ComponentName> getFilteredComponents() {
        if (!PredictionPreference.isEnabled(mContext)) {
            return Collections.emptyList();
        }
        List<ComponentName> components = getSortedComponents();
        for (int i = components.size() - 1; i >= 0; i--) {
            if (shouldFilterComponent(components.get(i))) {
                components.remove(i);
            }
        }
        // Remove the first few entries, as these easy to reach from recents.
        // Only a maximum of mCount is left.
        if (components.size() > mCount) {
            int removeEntries = Math.min(components.size() - mCount, MAX_REMOVE_TOP);
            components = components.subList(removeEntries, mCount + removeEntries);
        }
        return components;
    }

    /**
     * Checks if this activity should never be shown in suggestions.
     * @param cn The component name of the activity to check.
     * @return true if this should be filtered from suggestions, false otherwise.
     */
    private boolean shouldFilterComponent(ComponentName cn) {
        // Remove launcher starting shortcut itself.
        if (BuildConfig.APPLICATION_ID.equals(cn.getPackageName())) {
            return true;
        }

        Intent intent = new Intent().setPackage(cn.getPackageName());
        for (ResolveInfo ri : getPm().queryIntentActivities(intent, GET_RESOLVED_FILTER)) {
            ActivityInfo ai = ri.activityInfo;
            if (ai != null && cn.getClassName().equals(ai.name)) {
                // Found filter of right activity.
                IntentFilter filter = ri.filter;
                for (int i = 0; i < filter.countActions(); i++) {
                    if (FILTER_META.contains(filter.getAction(i))) {
                        return true;
                    }
                }
                for (int i = 0; i < filter.countCategories(); i++) {
                    if (FILTER_META.contains(filter.getCategory(i))) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }
}
