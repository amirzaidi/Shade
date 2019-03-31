package amirz.shade;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import amirz.shade.allapps.search.AppsSearchContainerLayout;

/**
 * Proxy activity that broadcasts search events to receivers.
 * Cannot be accessed from outside the launcher package.
 */
public class ShadeSearch extends Activity {
    private static final String ACTION = "amirz.shade.Search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();

        sendBroadcast(getIntent().setComponent(null).setAction(ACTION));
    }

    public static class Receiver extends BroadcastReceiver {
        private final AppsSearchContainerLayout mSearch;

        public Receiver(AppsSearchContainerLayout search) {
            mSearch = search;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String search = intent.getStringExtra("search");
            if (search != null) {
                mSearch.searchString(search);
            }
        }

        public void register(Context context) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION);
            context.registerReceiver(this, filter);
        }

        public void unregister(Context context) {
            context.unregisterReceiver(this);
        }
    }
}
