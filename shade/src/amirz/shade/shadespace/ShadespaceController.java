package amirz.shade.shadespace;

import android.os.Bundle;

import com.android.launcher3.plugin.unread.UnreadPluginClient;

import java.util.List;

class ShadespaceController {
    private final ShadespaceView mView;
    private final UnreadPluginClient mPlugin;

    ShadespaceController(ShadespaceView view, UnreadPluginClient plugin) {
        mView = view;
        mPlugin = plugin;
    }

    void updateView() {
        List<String> pluginText = mPlugin.getText();
        if (!pluginText.isEmpty()) {
            mView.setTopText(pluginText.get(0));
            if (pluginText.size() == 1) {
                mView.setBottomText("");
            } else if (pluginText.size() == 2) {
                mView.setBottomText(pluginText.get(1));
            } else {
                mView.setBottomTextSplit(pluginText.get(1), pluginText.get(2));
            }
        }
    }

    void clickView(Bundle activityOptions) {
        mPlugin.clickView(0, activityOptions);
    }
}
