package amirz.shade.hidden;

import android.content.Context;
import android.os.Process;

import com.android.launcher3.AppInfo;
import com.android.launcher3.R;
import com.android.launcher3.allapps.AppInfoComparator;
import com.android.launcher3.allapps.search.AllAppsSearchBarController;
import com.android.launcher3.allapps.search.DefaultAppSearchAlgorithm;
import com.android.launcher3.util.ComponentKey;
import com.android.launcher3.util.ItemInfoMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HiddenAppsSearchAlgorithm extends DefaultAppSearchAlgorithm {
    private final Context mContext;
    private final AppInfoComparator mAppNameComparator;
    private final Collection<AppInfo> mApps;
    private final String mKeyGlobal;
    private final String mKeyTranslated;

    public HiddenAppsSearchAlgorithm(Context context, Collection<AppInfo> apps) {
        super(Collections.emptyList());
        mContext = context;
        mAppNameComparator = new AppInfoComparator(context);
        mApps = apps;
        mKeyGlobal = context.getString(R.string.search_hidden_global).toLowerCase();
        mKeyTranslated = context.getString(R.string.search_hidden).toLowerCase();
    }

    @Override
    public void doSearch(final String query,
                         final AllAppsSearchBarController.Callbacks callback) {
        String trimmed = query.trim();
        String trimmedLowerCase = trimmed.toLowerCase();
        boolean showHidden = trimmedLowerCase.equals(mKeyGlobal)
                || trimmedLowerCase.equals(mKeyTranslated);
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
        final List<AppInfo> result = new ArrayList<>();
        StringMatcher matcher = StringMatcher.getInstance();
        for (AppInfo info : mApps) {
            if (matches(info, queryTextLower, matcher) || (showHidden
                    && HiddenAppsDatabase.isHidden(mContext, info.componentName, info.user))) {
                result.add(info);
            }
        }
        Collections.sort(result, mAppNameComparator);
        ArrayList<ComponentKey> resultKeys = new ArrayList<>();
        for (AppInfo appInfo : result) {
            resultKeys.add(appInfo.toComponentKey());
        }
        return resultKeys;
    }
}
