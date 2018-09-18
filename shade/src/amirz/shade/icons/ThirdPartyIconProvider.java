package amirz.shade.icons;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.graphics.drawable.Drawable;

import com.android.launcher3.graphics.DrawableFactory;

import amirz.shade.icons.pack.IconPackManager;
//import amirz.shade.icons.pack.IconReloader;
import amirz.shade.icons.pack.IconResolver;

public class ThirdPartyIconProvider extends RoundIconProvider {
    private final Context mContext;
    private IconPackManager mManager;
    private ThirdPartyDrawableFactory mFactory;
    //private IconReloader mReloader;

    public ThirdPartyIconProvider(Context context) {
        super(context);
        mContext = context;
        mManager = IconPackManager.get(context);
        mFactory = (ThirdPartyDrawableFactory) DrawableFactory.get(context);
        //mReloader = IconReloader.get(context);

        /*
        mDateChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!Utilities.ATLEAST_NOUGAT) {
                    int dateOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
                    if (dateOfMonth == mDateOfMonth) {
                        return;
                    }
                    mDateOfMonth = dateOfMonth;
                }
                LauncherAppsCompat apps = LauncherAppsCompat.getInstance(mContext);
                LauncherModel model = LauncherAppState.getInstance(context).getModel();
                DeepShortcutManager shortcutManager = DeepShortcutManager.getInstance(context);
                for (UserHandle user : UserManagerCompat.getInstance(context).getUserProfiles()) {
                    Set<String> packages = new HashSet<>();
                    for (ComponentName componentName : mFactory.packCalendars.keySet()) {
                        String pkg = componentName.getPackageName();
                        if (!apps.getActivityList(pkg, user).isEmpty()) {
                            packages.add(pkg);
                        }
                    }
                    for (String pkg : packages) {
                        IconReloader.reload(shortcutManager, model, user, pkg);
                    }
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIME_CHANGED);
        intentFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        if (!Utilities.ATLEAST_NOUGAT) {
            intentFilter.addAction(Intent.ACTION_TIME_TICK);
        }
        mContext.registerReceiver(mDateChangeReceiver, intentFilter, null, new Handler(LauncherModel.getWorkerLooper()));
        */
    }

    @Override
    public Drawable getIcon(LauncherActivityInfo launcherActivityInfo, int iconDpi, boolean flattenDrawable) {
        IconResolver resolver = mManager.resolve(
                launcherActivityInfo.getComponentName(), launcherActivityInfo.getUser());

        Drawable icon = resolver.useDefault()
                ? null
                : resolver.getIcon(iconDpi);

        return icon == null
                ? super.getIcon(launcherActivityInfo, iconDpi, flattenDrawable)
                : icon;
    }
}
