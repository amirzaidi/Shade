package amirz.plugin.shortcuts;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;

import com.android.launcher3.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.URI_INTENT_SCHEME;

public class SerializableShortcut {
    private final String mPackageName;
    private final ComponentName mActivity;

    private final String mId;
    private final String mComponentPackage;
    private final boolean mEnabled;
    private final Integer mIcon;
    private final String mShortLabel;
    private final String mLongLabel;
    private final String mDisabledMessage;

    private final Intent mIntent;

    SerializableShortcut(Resources res, ComponentName activity, XmlResourceParser xml)
            throws XmlPullParserException, IOException {
        mPackageName = activity.getPackageName();
        mActivity = activity;

        HashMap<String, String> data = new HashMap<>();
        for (int i = 0; i < xml.getAttributeCount(); i++) {
            data.put(xml.getAttributeName(i), xml.getAttributeValue(i));
        }

        mId = data.get("shortcutId");

        mEnabled = !data.containsKey("enabled")
                || data.get("enabled").toLowerCase().equals("true");

        if (data.containsKey("icon")) {
            String icon = data.get("icon");
            int resId = res.getIdentifier(icon, null, mPackageName);
            mIcon = resId == 0
                    ? Integer.parseInt(icon.substring(1))
                    : resId;
        } else {
            mIcon = 0;
        }

        mShortLabel = data.containsKey("shortcutShortLabel") ?
                res.getString(Integer.valueOf(data.get("shortcutShortLabel").substring(1))) :
                "";

        mLongLabel = data.containsKey("shortcutLongLabel") ?
                res.getString(Integer.valueOf(data.get("shortcutLongLabel").substring(1))) :
                mShortLabel;

        mDisabledMessage = data.containsKey("shortcutDisabledMessage") ?
                res.getString(Integer.valueOf(data.get("shortcutDisabledMessage").substring(1))) :
                "";

        HashMap<String, String> dataIntent = new HashMap<>();
        HashMap<String, String> dataExtras = new HashMap<>();
        HashMap<String, String> extras = new HashMap<>();
        int startDepth = xml.getDepth();
        do {
            if (xml.nextToken() == XmlPullParser.START_TAG) {
                String xmlName = xml.getName();
                if (xmlName.equals("intent")) {
                    dataIntent.clear();
                    extras.clear();
                    for (int i = 0; i < xml.getAttributeCount(); i++) {
                        dataIntent.put(xml.getAttributeName(i), xml.getAttributeValue(i));
                    }
                } else if (xmlName.equals("extra")) {
                    dataExtras.clear();
                    for (int i = 0; i < xml.getAttributeCount(); i++) {
                        dataExtras.put(xml.getAttributeName(i), xml.getAttributeValue(i));
                    }
                    if (dataExtras.containsKey("name") && dataExtras.containsKey("value")) {
                        extras.put(dataExtras.get("name"), dataExtras.get("value"));
                    }
                }
            }
        } while (xml.getDepth() > startDepth);

        String action = dataIntent.containsKey("action") ?
                dataIntent.get("action") :
                Intent.ACTION_MAIN;

        boolean useTargetPackage = dataIntent.containsKey("targetPackage");
        mComponentPackage = useTargetPackage ?
                dataIntent.get("targetPackage") :
                mPackageName;

        mIntent = new Intent(action)
                .setPackage(mComponentPackage)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_TASK_ON_HOME);

        if (dataIntent.containsKey("targetClass")) {
            mIntent.setComponent(new ComponentName(mComponentPackage, dataIntent.get("targetClass")));
        }

        if (dataIntent.containsKey("data")) {
            mIntent.setData(Uri.parse(dataIntent.get("data")));
        }

        for (Map.Entry<String, String> entry : extras.entrySet()) {
            mIntent.putExtra(entry.getKey(), entry.getValue());
        }
    }

    private Drawable getIcon(Context context, int density) {
        try {
            return context.getPackageManager()
                    .getResourcesForApplication(mPackageName)
                    .getDrawableForDensity(mIcon, density);
        } catch (PackageManager.NameNotFoundException | Resources.NotFoundException ignored) {
            return context.getResources().getDrawableForDensity(
                    R.drawable.ic_default_shortcut, density);
        }
    }

    private ComponentName getActivity() {
        return mIntent.getComponent() == null ? mActivity : mIntent.getComponent();
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(mId) && !TextUtils.isEmpty(mShortLabel);
    }

    public Intent getIntent() {
        return mIntent;
    }

    public String getKey() {
        return getActivity().flattenToShortString() + "/" + mId;
    }

    Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putString("key", getKey());
        bundle.putString("id", mId);
        bundle.putString("intent", mIntent.toUri(URI_INTENT_SCHEME));
        bundle.putString("package", mComponentPackage);
        bundle.putString("activity", getActivity().flattenToShortString());
        bundle.putString("shortLabel", mShortLabel);
        bundle.putString("longLabel", mLongLabel);
        bundle.putString("disabledMessage", mDisabledMessage);
        bundle.putParcelable("userHandle", Process.myUserHandle());
        bundle.putInt("rank", -1);
        bundle.putBoolean("enabled", mEnabled);
        return bundle;
    }

    Bitmap getBitmap(Context context, int density) {
        Drawable icon = getIcon(context, density);
        Bitmap bitmap = Bitmap.createBitmap(
                icon.getIntrinsicWidth(), icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        icon.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        icon.draw(canvas);
        return bitmap;
    }
}
