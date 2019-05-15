package amirz.shade.customization;

import android.content.Context;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.util.DisplayMetrics;
import android.util.Xml;

import com.android.launcher3.InvariantDeviceProfile;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import amirz.shade.ShadeSettings;

public class GridOverride extends InvariantDeviceProfile.ProfileOverride {
    private static final List<GridProfile> sGridProfiles = new ArrayList<>();

    private static void parseProfiles(Context context) throws IOException, XmlPullParserException {
        try (XmlResourceParser parser = context.getResources().getXml(R.xml.grid_profiles)) {
            final int depth = parser.getDepth();
            int type;

            while (((type = parser.next()) != XmlPullParser.END_TAG ||
                    parser.getDepth() > depth) && type != XmlPullParser.END_DOCUMENT) {
                if ((type == XmlPullParser.START_TAG) && "profile".equals(parser.getName())) {
                    TypedArray a = context.obtainStyledAttributes(
                            Xml.asAttributeSet(parser), R.styleable.InvariantDeviceProfile);
                    int numRows = a.getInt(R.styleable.InvariantDeviceProfile_numRows, 0);
                    int numColumns = a.getInt(R.styleable.InvariantDeviceProfile_numColumns, 0);
                    float iconSize = a.getFloat(R.styleable.InvariantDeviceProfile_iconSize, 0);
                    sGridProfiles.add(new GridProfile(
                            a.getString(R.styleable.InvariantDeviceProfile_name),
                            numRows,
                            numColumns,
                            a.getInt(R.styleable.InvariantDeviceProfile_numFolderRows, numRows),
                            a.getInt(R.styleable.InvariantDeviceProfile_numFolderColumns, numColumns),
                            iconSize,
                            a.getFloat(R.styleable.InvariantDeviceProfile_landscapeIconSize, iconSize),
                            a.getFloat(R.styleable.InvariantDeviceProfile_iconTextSize, 0)));
                    a.recycle();
                }
            }
        }
    }

    public GridOverride(Context context) {
        if (sGridProfiles.isEmpty()) {
            try {
                parseProfiles(context);
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
        }
    }

    protected void apply(Context context, InvariantDeviceProfile inv, DisplayMetrics dm) {
        String name = Utilities.getPrefs(context).getString(ShadeSettings.PREF_GRID_SIZE, "");
        for (GridProfile profile : sGridProfiles) {
            if (name.equals(profile.name)) {
                inv.numRows = profile.numRows;
                inv.numColumns = profile.numColumns;
                inv.numHotseatIcons = profile.numColumns;
                inv.numFolderRows = profile.numFolderRows;
                inv.numFolderColumns = profile.numFolderColumns;
                inv.iconSize = profile.iconSize;
                inv.landscapeIconSize = profile.landscapeIconSize;
                inv.iconTextSize = profile.iconTextSize;
                break;
            }
        }
    }

    private static class GridProfile {
        private final String name;
        private final int numRows;
        private final int numColumns;
        private final int numFolderRows;
        private final int numFolderColumns;
        private final float iconSize;
        private final float landscapeIconSize;
        private final float iconTextSize;

        private GridProfile(String name, int numRows, int numColumns,
                            int numFolderRows, int numFolderColumns,
                            float iconSize, float landscapeIconSize, float iconTextSize) {
            this.name = name;
            this.numRows = numRows;
            this.numColumns = numColumns;
            this.numFolderRows = numFolderRows;
            this.numFolderColumns = numFolderColumns;
            this.iconSize = iconSize;
            this.landscapeIconSize = landscapeIconSize;
            this.iconTextSize = iconTextSize;
        }
    }
}
