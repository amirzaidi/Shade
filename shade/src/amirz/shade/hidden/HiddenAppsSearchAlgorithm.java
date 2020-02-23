package amirz.shade.hidden;

import android.content.Context;

import com.android.launcher3.AppInfo;
import com.android.launcher3.R;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm;
import com.android.launcher3.util.ComponentKey;

import java.util.ArrayList;
import java.util.List;

public class HiddenAppsSearchAlgorithm extends DefaultAppSearchAlgorithm {
    private final Context mContext;
    private final List<AppInfo> mApps;
    private final String mKeyword;

    public HiddenAppsSearchAlgorithm(Context context, List<AppInfo> apps) {
        super(apps);
        mContext = context;
        mApps = apps;
        mKeyword = context.getString(R.string.search_hidden).toLowerCase();
    }

    @Override
    public void doSearch(final String query,
                         final AllAppsSearchBarController.Callbacks callback) {
        String trimmed = query.trim();
        boolean showHidden = trimmed.toLowerCase().equals(mKeyword);
        if (showHidden) {
            HiddenAppsDrawerState.getInstance(mContext).setRevealed(true);
        }
        final ArrayList<ComponentKey> result = getTitleMatchResult(query, showHidden);
        mResultHandler.post(() -> callback.onSearchResult(query, result));
    }

    private ArrayList<ComponentKey> getTitleMatchResult(String query, boolean showHidden) {
        // Do an intersection of the words in the query and each title, and filter out all the
        // apps that don't match all of the words in the query.
        final String queryTextLower = query.toLowerCase();
        final ArrayList<ComponentKey> result = new ArrayList<>();
        StringMatcher matcher = StringMatcher.getInstance();
        for (AppInfo info : mApps) {
            if (matches(info, queryTextLower, matcher) || (showHidden
                    && HiddenAppsDatabase.isHidden(mContext, info.componentName, info.user))) {
                result.add(info.toComponentKey());
            }
        }
        return result;
    }
}
