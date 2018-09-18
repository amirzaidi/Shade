package amirz.shade.icons.pack;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.SparseArray;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that handles the metadata and data of any icon pack.
 * It uses a WeakReference to the data for reduced memory consumption, by letting the garbage
 * collector handle it.
 */
class IconPack {
    private final ApplicationInfo mAi;
    private final CharSequence mPackageLabel;
    private WeakReference<Data> mData;

    IconPack(ApplicationInfo ai, CharSequence label) {
        mAi = ai;
        mPackageLabel = label;
        mData = new WeakReference<>(null);
    }

    ApplicationInfo getAi() {
        return mAi;
    }

    String getPackage() {
        return mAi.packageName;
    }

    CharSequence getTitle() {
        return mPackageLabel;
    }

    Data getData(PackageManager pm)
            throws PackageManager.NameNotFoundException, XmlPullParserException, IOException {
        Data data = mData.get();
        if (data == null) {
            data = IconPackParser.parsePackage(pm, getPackage());
            mData = new WeakReference<>(data);
        }
        return data;
    }

    static class Data {
        final Map<ComponentName, Integer> drawables = new HashMap<>();
        final Map<ComponentName, String> calendarPrefix = new HashMap<>();
        final SparseArray<Clock> clockMetadata = new SparseArray<>();
    }

    static class Clock {
        final int hourLayerIndex;
        final int minuteLayerIndex;
        final int secondLayerIndex;
        final int defaultHour;
        final int defaultMinute;
        final int defaultSecond;

        Clock(int hourLayerIndex, int minuteLayerIndex, int secondLayerIndex,
              int defaultHour, int defaultMinute, int defaultSecond) {
            this.hourLayerIndex = hourLayerIndex;
            this.minuteLayerIndex = minuteLayerIndex;
            this.secondLayerIndex = secondLayerIndex;
            this.defaultHour = defaultHour;
            this.defaultMinute = defaultMinute;
            this.defaultSecond = defaultSecond;
        }
    }
}
