package amirz.shortcut;

import android.app.LauncherActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.ListView;

import java.util.Collections;
import java.util.List;

public class CreateShortcut extends LauncherActivity {
    @Override
    protected Intent getTargetIntent() {
        return new Intent(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    }

    @Override
    public List<ListItem> makeListItems() {
        List<ListItem> items = super.makeListItems();
        Collections.sort(items, (o1, o2) -> pkgLabel(o1).compareTo(pkgLabel(o2)));
        return items;
    }

    private String pkgLabel(ListItem item) {
        return item.resolveInfo.activityInfo.applicationInfo
                .loadLabel(getPackageManager()).toString();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final ListItem item = itemForPosition(position);
        Intent shortcutIntent = intentForPosition(position);

        Intent.ShortcutIconResource iconResource = null;
        try {
            Context pkgContext = createPackageContext(item.packageName, 0);
            iconResource = Intent.ShortcutIconResource.fromContext(
                    pkgContext, item.resolveInfo.getIconResource());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                .putExtra(Intent.EXTRA_SHORTCUT_NAME, item.label)
                .putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        setResult(RESULT_OK, intent);
        finish();
    }
}
