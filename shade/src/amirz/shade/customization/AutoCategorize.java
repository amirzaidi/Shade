package amirz.shade.customization;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.content.pm.ResolveInfo;
import android.os.Process;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.provider.Settings;

import com.android.launcher3.util.ComponentKey;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@SuppressLint("InlinedApi")
class AutoCategorize {
    private static final String CATEGORY_MEDIA = "media";
    private static final String CATEGORY_SOCIAL = "social";
    private static final String CATEGORY_UTILITY = "utility";
    private static final String CATEGORY_TRAVEL = "travel";

    private static final Map<String, String> ORDERED_MAP = new LinkedHashMap<>();

    static {
        // Most specific should be on top.
        ORDERED_MAP.put(Settings.ACTION_SETTINGS, CATEGORY_UTILITY);
        ORDERED_MAP.put(AlarmClock.ACTION_SET_ALARM, CATEGORY_UTILITY);
        ORDERED_MAP.put(AlarmClock.ACTION_SET_TIMER, CATEGORY_UTILITY);

        // Simple to categorize because of their category tags.
        ORDERED_MAP.put(Intent.CATEGORY_APP_MAPS, CATEGORY_TRAVEL);
        ORDERED_MAP.put(Intent.CATEGORY_CAR_MODE, CATEGORY_TRAVEL);
        ORDERED_MAP.put(Intent.CATEGORY_CAR_DOCK, CATEGORY_TRAVEL);
        ORDERED_MAP.put(Intent.CATEGORY_APP_MUSIC, CATEGORY_MEDIA);
        ORDERED_MAP.put(Intent.CATEGORY_APP_BROWSER, CATEGORY_UTILITY);
        ORDERED_MAP.put(Intent.CATEGORY_APP_CALENDAR, CATEGORY_UTILITY);
        ORDERED_MAP.put(Intent.CATEGORY_APP_CALCULATOR, CATEGORY_UTILITY);
        ORDERED_MAP.put(Intent.CATEGORY_APP_MARKET, CATEGORY_UTILITY);
        ORDERED_MAP.put(Intent.CATEGORY_HOME, CATEGORY_UTILITY);
        ORDERED_MAP.put(Intent.CATEGORY_APP_CONTACTS, CATEGORY_SOCIAL);
        ORDERED_MAP.put(Intent.CATEGORY_APP_EMAIL, CATEGORY_SOCIAL);
        ORDERED_MAP.put(Intent.CATEGORY_APP_MESSAGING, CATEGORY_SOCIAL);
        ORDERED_MAP.put(Intent.ACTION_DIAL, CATEGORY_SOCIAL);
        ORDERED_MAP.put(Intent.CATEGORY_APP_GALLERY, CATEGORY_MEDIA);
        ORDERED_MAP.put(Intent.CATEGORY_INFO, CATEGORY_UTILITY);

        // Lesser used permissions.
        ORDERED_MAP.put(Manifest.permission.WRITE_CALENDAR, CATEGORY_UTILITY);
        ORDERED_MAP.put(Manifest.permission.MANAGE_OWN_CALLS, CATEGORY_SOCIAL);
        ORDERED_MAP.put(Manifest.permission.READ_PHONE_STATE, CATEGORY_SOCIAL);
        ORDERED_MAP.put(Manifest.permission.READ_PHONE_NUMBERS, CATEGORY_SOCIAL);
        ORDERED_MAP.put(Manifest.permission.CALL_PHONE, CATEGORY_SOCIAL);
        ORDERED_MAP.put(Manifest.permission.WRITE_SECURE_SETTINGS, CATEGORY_UTILITY);
        ORDERED_MAP.put(Manifest.permission.WRITE_CONTACTS, CATEGORY_SOCIAL);
        ORDERED_MAP.put(Manifest.permission.REQUEST_INSTALL_PACKAGES, CATEGORY_UTILITY);

        // Fall back to media category when it uses the camera.
        ORDERED_MAP.put(MediaStore.ACTION_IMAGE_CAPTURE, CATEGORY_MEDIA);
        ORDERED_MAP.put(MediaStore.ACTION_IMAGE_CAPTURE_SECURE, CATEGORY_MEDIA);
        ORDERED_MAP.put(MediaStore.ACTION_VIDEO_CAPTURE, CATEGORY_MEDIA);
        ORDERED_MAP.put(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA, CATEGORY_MEDIA);
        ORDERED_MAP.put(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE, CATEGORY_MEDIA);
        ORDERED_MAP.put(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH, CATEGORY_MEDIA);
        ORDERED_MAP.put(MediaStore.INTENT_ACTION_MEDIA_SEARCH, CATEGORY_MEDIA);
        ORDERED_MAP.put(MediaStore.INTENT_ACTION_VIDEO_CAMERA, CATEGORY_MEDIA);
        ORDERED_MAP.put(MediaStore.INTENT_ACTION_VIDEO_PLAY_FROM_SEARCH, CATEGORY_MEDIA);
        ORDERED_MAP.put(Manifest.permission.CAMERA, CATEGORY_MEDIA);

        // Almost every app uses these.
        ORDERED_MAP.put(Manifest.permission.READ_CONTACTS, CATEGORY_SOCIAL);
    }

    static String getCategory(Context context, ComponentKey key) {
        // Work apps are in travel by default.
        if (key.user != Process.myUserHandle()) {
            return CATEGORY_TRAVEL;
        }

        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent().setPackage(key.componentName.getPackageName());

        Set<String> meta = new HashSet<>();
        for (ResolveInfo ri : pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER)) {
            IntentFilter filter = ri.filter;
            for (int i = 0; i < filter.countActions(); i++) {
                meta.add(filter.getAction(i));
            }
            for (int i = 0; i < filter.countCategories(); i++) {
                meta.add(filter.getCategory(i));
            }
        }
        try {
            PackageInfo pi = pm.getPackageInfo(key.componentName.getPackageName(),
                    PackageManager.GET_PERMISSIONS);
            if (pi.permissions != null) {
                for (PermissionInfo perm : pi.permissions) {
                    meta.add(perm.name);
                }
            }
            if (pi.requestedPermissions != null) {
                meta.addAll(Arrays.asList(pi.requestedPermissions));
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        for (Map.Entry<String, String> kvp : ORDERED_MAP.entrySet()) {
            if (meta.contains(kvp.getKey())) {
                return kvp.getValue();
            }
        }

        // The default category when there are no matches is utility.
        return CATEGORY_UTILITY;
    }
}
