package amirz.shade.appprediction;

import android.annotation.SuppressLint;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

class UsageTracker {
    private static final int MAX_ENTRIES = 20;

    private final PackageManager mPm;
    private final UsageStatsManager mManager;

    @SuppressLint("WrongConstant")
    UsageTracker(Context context) {
        mPm = context.getPackageManager();
        mManager = (UsageStatsManager) context.getSystemService("usagestats");
    }

    List<ComponentName> getSortedComponents() {
        List<ComponentName> components = new ArrayList<>();
        for (String pkg : getSortedPackages()) {
            Intent intent = mPm.getLaunchIntentForPackage(pkg);
            if (intent != null) {
                components.add(intent.getComponent());
            }
        }
        return components;
    }

    PackageManager getPm() {
        return mPm;
    }

    private List<String> getSortedPackages() {
        List<String> packages = new ArrayList<>();
        for (UsageStats stat : getSortedStats()) {
            packages.add(stat.getPackageName());
        }
        return packages;
    }

    private List<UsageStats> getSortedStats() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -1);

        List<UsageStats> stats = mManager.queryUsageStats(UsageStatsManager.INTERVAL_YEARLY,
                calendar.getTimeInMillis(), System.currentTimeMillis());
        SortedMap<Long, UsageStats> sorted = new TreeMap<>();
        for (UsageStats usageStats : stats) {
            sorted.put(-usageStats.getLastTimeUsed(), usageStats);
        }

        Set<String> packages = new HashSet<>();
        List<UsageStats> sortedList = new ArrayList<>();
        for (UsageStats stat : sorted.values()) {
            // Prevent slowing down the phone too much.
            if (sortedList.size() == MAX_ENTRIES) {
                break;
            }
            String pkg = stat.getPackageName();
            if (!packages.contains(pkg)) {
                packages.add(pkg);
                sortedList.add(stat);
            }
        }

        return sortedList;
    }
}
