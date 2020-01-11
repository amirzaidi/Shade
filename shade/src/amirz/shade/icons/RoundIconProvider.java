package amirz.shade.icons;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.Drawable;

import com.android.launcher3.IconProvider;

import org.xmlpull.v1.XmlPullParser;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts roundIcon attribute from AndroidManifest.xml, then uses it as the drawer icon.
 */
public class RoundIconProvider extends IconProvider {
    private final Context mContext;

    @SuppressWarnings("WeakerAccess")
    public RoundIconProvider(Context context) {
        mContext = context;
    }

    @Override
    public Drawable getIcon(LauncherActivityInfo launcherActivityInfo, int iconDpi, boolean flattenDrawable) {
        ComponentName component = launcherActivityInfo.getComponentName();
        Drawable roundIcon = getRoundIcon(component, iconDpi);

        // Fall back on the default icon if round icon extraction fails.
        return roundIcon == null
                ? super.getIcon(launcherActivityInfo, iconDpi, flattenDrawable)
                : roundIcon;
    }

    private Drawable getRoundIcon(ComponentName component, int iconDpi) {
        String pkg = component.getPackageName();
        String appIcon = null;
        Map<String, String> elementTags = new HashMap<>();

        try {
            Resources res = mContext.getPackageManager().getResourcesForApplication(pkg);
            AssetManager assets = res.getAssets();

            XmlResourceParser parseXml = assets.openXmlResourceParser("AndroidManifest.xml");
            while (parseXml.next() != XmlPullParser.END_DOCUMENT) {
                if (parseXml.getEventType() == XmlPullParser.START_TAG) {
                    String name = parseXml.getName();
                    for (int i = 0; i < parseXml.getAttributeCount(); i++) {
                        elementTags.put(parseXml.getAttributeName(i), parseXml.getAttributeValue(i));
                    }
                    if (elementTags.containsKey("icon")) {
                        if (name.equals("application")) {
                            appIcon = elementTags.get("roundIcon");
                        } else if ((name.equals("activity") || name.equals("activity-alias")) &&
                                elementTags.containsKey("name") &&
                                elementTags.get("name").equals(component.getClassName())) {
                            appIcon = elementTags.get("roundIcon");
                            break;
                        }
                    }
                    elementTags.clear();
                }
            }
            parseXml.close();

            if (appIcon != null) {
                // Sometimes this is a reference, then we need to use getIdentifier
                // to get the actual resource id.
                int resId = res.getIdentifier(appIcon, null, pkg);
                return res.getDrawableForDensity(resId == 0
                        ? Integer.parseInt(appIcon.substring(1))
                        : resId, iconDpi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
