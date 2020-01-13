package amirz.shade.customization;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.text.format.DateUtils;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import java.io.File;

import static android.content.pm.PackageManager.GET_META_DATA;

class MetadataExtractor {
    private final Context mContext;
    private final PackageManager mPm;

    private String mSourcePkg = "";
    private long mLastUpdate = 0;
    private String mVersionName;
    private long mVersionCode = 0;

    MetadataExtractor(Context context, ComponentName cn) {
        mContext = context;
        mPm = context.getPackageManager();
        String pkg = cn.getPackageName();

        try {
            mSourcePkg = mPm.getInstallerPackageName(pkg);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        try {
            PackageInfo pi = mPm.getPackageInfo(pkg, GET_META_DATA);
            mLastUpdate = new File(pi.applicationInfo.sourceDir).lastModified();
            mVersionName = pi.versionName;
            mVersionCode = Utilities.ATLEAST_P ? pi.getLongVersionCode() : pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    CharSequence getSource() {
        if (TextUtils.isEmpty(mSourcePkg)) {
            return mContext.getString(R.string.app_info_source_unknown);
        }
        try {
            ApplicationInfo pi = mPm.getApplicationInfo(mSourcePkg, 0);
            return pi.loadLabel(mPm);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return mSourcePkg;
    }

    String getLastUpdate() {
        return DateUtils.formatDateTime(mContext, mLastUpdate,
                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE);
    }

    String getVersionName() {
        return TextUtils.isEmpty(mVersionName)
                ? mContext.getString(R.string.app_info_source_unknown)
                : mVersionName;
    }

    long getVersionCode() {
        return mVersionCode;
    }
}
