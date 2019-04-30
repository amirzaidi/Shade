package amirz.shade;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.android.launcher3.BuildConfig;
import com.android.launcher3.R;
import com.android.launcher3.SettingsActivity;
import com.android.launcher3.Utilities;
import com.android.launcher3.uioverrides.WallpaperColorInfo;
import com.android.quickstep.QuickstepProcessInitializer;

import amirz.shade.customization.AppReloader;
import amirz.shade.customization.GlobalIconPackPreference;

public class ShadeSettings extends SettingsActivity {
    public static final String PREF_FEED_PROVIDER = "pref_feed_provider";
    public static final String PREF_THEME = "pref_theme";
    public static final String PREF_ICON_PACK = "pref_icon_pack";
    public static final String PREF_TRANSITION = "pref_transition";
    public static final String PREF_UNREAD = "pref_unread";
    private static final String ABOUT_APP_VERSION = "about_app_version";
    private static final int UPDATE_THEME_DELAY = 500;
    private static final int CLOSE_STACK_DELAY = 500;
    private boolean mReloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                getPreferenceScreen().removePreference(findPreference(PREF_TRANSITION));
            }

            Preference version = findPreference(ABOUT_APP_VERSION);
            version.setSummary(BuildConfig.VERSION_NAME);
            Uri intentData = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
            version.setIntent(version.getIntent().setData(intentData));
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
        switch (Utilities.getPrefs(context).getString(ShadeSettings.PREF_THEME, "")) {
            case "transparent": return R.style.Shade_Transparent;
            case "sunset": return R.style.Shade_Sunset;
            case "campfire": return R.style.Shade_Campfire;
            case "sunrise": return R.style.Shade_Sunrise;
            case "forest": return R.style.Shade_Forest;
            case "ocean": return R.style.Shade_Ocean;
            case "twilight": return R.style.Shade_Twilight;
            case "blossom": return R.style.Shade_Blossom;
            case "midnight": return R.style.Shade_Midnight;
        }
        return defValue;
    }
}
