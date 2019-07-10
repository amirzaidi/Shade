package amirz.shade;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;
import com.android.launcher3.SettingsActivity;
import com.android.launcher3.Utilities;
import com.android.launcher3.plugin.PluginManager;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.quickstep.QuickstepProcessInitializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import amirz.shade.customization.AppReloader;
import amirz.shade.customization.GlobalIconPackPreference;

public class ShadeSettings extends SettingsActivity {
    public static final String PREF_FEED_PROVIDER = "pref_feed_provider";
    public static final String PREF_THEME = "pref_theme";
    public static final String PREF_ICON_PACK = "pref_icon_pack";
    public static final String PREF_DOCK_SEARCH = "pref_dock_search";
    public static final String PREF_GRID_SIZE = "pref_grid_size";
    public static final String PREF_TRANSITION = "pref_transition";
    private static final String ABOUT_APP_VERSION = "about_app_version";
    private static final String CATEGORY_CUSTOMIZATION = "category_customization";
    private static final String CATEGORY_PLUGINS = "category_plugins";
    private static final int UPDATE_THEME_DELAY = 500;
    private static final int CLOSE_STACK_DELAY = 500;
    private boolean mReloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ShadeFont.override(this);
        super.onCreate(savedInstanceState);

        // Load the Shade theme attributes
        Resources.Theme theme = getTheme();
        theme.applyStyle(getThemeRes(this, R.style.ShadeSettings_Default), false);
        theme.applyStyle(R.style.ShadeSettings_Override, true);
    }

    @Override
    protected PreferenceFragment getNewFragment() {
        return new ShadeFragment();
    }

    public static class ShadeFragment extends SettingsActivity.LauncherSettingsFragment {
        private final Handler mHandler = new Handler();
        private Context mContext;
        private GlobalIconPackPreference mIconPackPref;
        private ListPreference mThemePref;
        private ListPreference mFeedPref;
        private PluginManager mManager;
        private PreferenceCategory mCategory;

        @Override
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            mContext = getActivity();

            mIconPackPref = (GlobalIconPackPreference) findPreference(PREF_ICON_PACK);
            mIconPackPref.setOnPreferenceChangeListener((p, v) -> {
                Utilities.getPrefs(mContext).edit().putString(PREF_ICON_PACK, v.toString()).apply();
                AppReloader.get(mContext).reload();
                return true;
            });

            Runnable updateTheme = () ->
                    WallpaperColorInfo.getInstance(mContext).notifyChange();

            mThemePref = (ListPreference) findPreference(PREF_THEME);
            mThemePref.setOnPreferenceChangeListener((p, v) -> {
                Utilities.getPrefs(mContext).edit().putString(PREF_THEME, v.toString()).apply();

                mHandler.removeCallbacks(updateTheme);
                mHandler.postDelayed(updateTheme, UPDATE_THEME_DELAY);

                ShadeSettings activity = (ShadeSettings) mContext;
                activity.reload();
                return true;
            });

            if (QuickstepProcessInitializer.isEnabled()) {
                PreferenceCategory pc = (PreferenceCategory) findPreference(CATEGORY_CUSTOMIZATION);
                pc.removePreference(findPreference(PREF_TRANSITION));
            }

            mManager = PluginManager.getInstance(mContext);
            mCategory = (PreferenceCategory) findPreference(CATEGORY_PLUGINS);
            mFeedPref = (ListPreference) mCategory.findPreference(PREF_FEED_PROVIDER);

            Preference version = findPreference(ABOUT_APP_VERSION);
            version.setSummary(BuildConfig.VERSION_NAME);
            Uri intentData = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            version.setIntent(version.getIntent().setData(intentData));
        }

        @Override
        public void onResume() {
            super.onResume();

            mCategory.removeAll();
            mCategory.addPreference(mFeedPref);

            List<PluginManager.Plugin> pluginList = mManager.getPlugins();
            Collections.sort(pluginList, (o1, o2) -> name(o1).compareTo(name(o2)));

            List<Runnable> prefReload = new ArrayList<>();
            for (PluginManager.Plugin plugin : pluginList) {
                SwitchPreference pref = new SwitchPreference(mContext);
                pref.setTitle(name(plugin));
                pref.setSummary(plugin.getLongLabel());
                pref.setChecked(plugin.isEnabled());
                pref.setOnPreferenceChangeListener((b, v) -> {
                    plugin.setEnabled((boolean) v);
                    mManager.reloadConnections();

                    // Visually update all other plugin toggles.
                    for (Runnable runnable : prefReload) {
                        runnable.run();
                    }
                    return true;
                });

                // Save so it can be referenced by other toggles.
                prefReload.add(() -> pref.setChecked(plugin.isEnabled()));
                mCategory.addPreference(pref);
            }
        }

        private String name(PluginManager.Plugin plugin) {
            String appLabel = plugin.getAppLabel().toString();
            String shortLabel = plugin.getShortLabel().toString();
            return plugin.isInPackage()
                    ? shortLabel
                    : mContext.getString(R.string.plugin_title_long, appLabel, shortLabel);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getActivity().finishAndRemoveTask();
        }
    }

    private void reload() {
        mReloaded = true;
        Intent intent = getIntent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        new Handler().postDelayed(this::finish, CLOSE_STACK_DELAY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mReloaded) {
            // Close if we had already loaded another theme instance.
            finishAndRemoveTask();
        }
    }

    public static int getThemeRes(Context context, int defValue) {
        return getThemeRes(context, defValue, false);
    }

    public static int getThemeRes(Context context, int defValue, boolean darkText) {
        switch (Utilities.getPrefs(context).getString(ShadeSettings.PREF_THEME, "")) {
            case "transparent":
                return darkText ? R.style.Shade_Transparent_DarkText : R.style.Shade_Transparent;
            case "campfire":
                return darkText ? R.style.Shade_Campfire_DarkText : R.style.Shade_Campfire;
            case "sunset":
                return darkText ? R.style.Shade_Sunset_DarkText : R.style.Shade_Sunset;
            case "sunrise":
                return darkText ? R.style.Shade_Sunrise_DarkText : R.style.Shade_Sunrise;
            case "forest":
                return darkText ? R.style.Shade_Forest_DarkText : R.style.Shade_Forest;
            case "ocean":
                return darkText ? R.style.Shade_Ocean_DarkText : R.style.Shade_Ocean;
            case "twilight":
                return darkText ? R.style.Shade_Twilight_DarkText : R.style.Shade_Twilight;
            case "blossom":
                return darkText ? R.style.Shade_Blossom_DarkText : R.style.Shade_Blossom;
            case "midnight":
                return darkText ? R.style.Shade_Midnight_DarkText : R.style.Shade_Midnight;
        }
        return defValue;
    }
}
