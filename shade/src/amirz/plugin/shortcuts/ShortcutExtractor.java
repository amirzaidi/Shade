package amirz.plugin.shortcuts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Process;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ShortcutExtractor {
    private final Context mContext;
    private final LauncherApps mApps;
    private final Map<String, SerializableShortcut> mShortcutCache = new HashMap<>();

    ShortcutExtractor(Context context) {
        mContext = context;
        mApps = (LauncherApps) context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
    }

    List<Bundle> getForActivity(String packageName, ComponentName activity) {
        List<Bundle> out = new ArrayList<>();
        if (packageName != null) {
            List<LauncherActivityInfo> infoList =
                    mApps.getActivityList(packageName, Process.myUserHandle());
            for (LauncherActivityInfo info : infoList) {
                if (activity == null || activity.equals(info.getComponentName())) {
                    try {
                        parseActivity(info.getComponentName(), out);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return out;
    }

    Bitmap getIcon(String key, int density) {
        if (mShortcutCache.containsKey(key)) {
            return mShortcutCache.get(key).getBitmap(mContext, density);
        }
        return null;
    }

    private void parseActivity(ComponentName activity, List<Bundle> out)
            throws IOException, XmlPullParserException, PackageManager.NameNotFoundException {
        PackageManager pm = mContext.getPackageManager();

        String shortcutsRes = null;
        String currActivity = "";
        String searchActivity = activity.getClassName();

        Map<String, String> parsedData = new HashMap<>();

        Resources res = pm.getResourcesForApplication(activity.getPackageName());
        AssetManager assets = res.getAssets();
        XmlResourceParser xml = assets.openXmlResourceParser("AndroidManifest.xml");

        int eventType;
        while ((eventType = xml.nextToken()) != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                String name = xml.getName();
                if ("activity".equals(name) || "activity-alias".equals(name)) {
                    parsedData.clear();
                    for (int i = 0; i < xml.getAttributeCount(); i++) {
                        parsedData.put(xml.getAttributeName(i), xml.getAttributeValue(i));
                    }
                    if (parsedData.containsKey("name")) {
                        currActivity = parsedData.get("name");
                    }
                } else if (name.equals("meta-data") && searchActivity.equals(currActivity)) {
                    parsedData.clear();
                    for (int i = 0; i < xml.getAttributeCount(); i++) {
                        parsedData.put(xml.getAttributeName(i), xml.getAttributeValue(i));
                    }
                    if (parsedData.containsKey("name")
                            && "android.app.shortcuts".equals(parsedData.get("name"))
                            && parsedData.containsKey("resource")) {
                        shortcutsRes = parsedData.get("resource");
                    }
                }
            }
        }
        xml.close();

        if (shortcutsRes != null) {
            int resId = res.getIdentifier(shortcutsRes, null, activity.getPackageName());
            xml = res.getXml(resId == 0
                    ? Integer.parseInt(shortcutsRes.substring(1))
                    : resId);

            while ((eventType = xml.nextToken()) != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xml.getName().equals("shortcut")) {
                        try {
                            SerializableShortcut info = new SerializableShortcut(res, activity, xml);
                            if (info.isValid()) {
                                Intent intent = info.getIntent();
                                for (ResolveInfo ri : pm.queryIntentActivities(intent, 0)) {
                                    if (ri.isDefault || ri.activityInfo.exported) {
                                        mShortcutCache.put(info.getKey(), info);
                                        out.add(info.toBundle());
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            xml.close();
        }
    }
}
