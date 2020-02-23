package amirz.shade.hidden;

import android.content.Context;

import com.android.launcher3.AppInfo;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm;

import java.util.List;

public class HiddenAppsSearchAlgorithm extends DefaultAppSearchAlgorithm {
    private final Context mContext;

    public HiddenAppsSearchAlgorithm(Context context, List<AppInfo> apps) {
        super(apps);
        mContext = context;
    }

    @Override
    public void doSearch(final String query,
                         final AllAppsSearchBarController.Callbacks callback) {
        super.doSearch(query.trim(), callback);
    }
}
