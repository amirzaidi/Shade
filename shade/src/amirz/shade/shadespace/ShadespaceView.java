package amirz.shade.shadespace;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.android.launcher3.plugin.PluginManager;
import com.android.launcher3.plugin.unread.UnreadPluginClient;

public class ShadespaceView extends LinearLayout implements UnreadPluginClient.UnreadListener {
    private final UnreadPluginClient mPluginClient;
    private final ShadespaceController mController;

    private DoubleShadowTextView mTopView;
    private DoubleShadowTextView mBottomView;
    private boolean mRunning;

    @SuppressLint({"ClickableViewAccessibility"})
    public ShadespaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPluginClient = PluginManager.getInstance(context).getClient(UnreadPluginClient.class);
        mController = new ShadespaceController(this, mPluginClient);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mTopView = findViewById(R.id.shadespace_text);
        mBottomView = findViewById(R.id.shadespace_subtext);

        setOnClickListener(v -> mController.clickView(
                Launcher.getLauncher(getContext()).getActivityLaunchOptionsAsBundle(this)));
    }

    public void onResume() {
        if (!mRunning) {
            mRunning = true;
            mPluginClient.setListener(this);
            onChange();
        }
    }

    public void onPause() {
        if (mRunning) {
            mRunning = false;
            mPluginClient.setListener(null);
        }
    }

    public void setTopText(CharSequence s) {
        mTopView.setText(s);
    }

    public void setBottomText(CharSequence s) {
        mBottomView.setText(s);
    }

    public void setBottomTextSplit(CharSequence s1, CharSequence s2) {
        mBottomView.setText(getContext().getString(R.string.shadespace_subtext_double, s1, s2));
    }

    @Override
    public void onChange() {
        // Do not update the content when we are paused.
        // This prevents the text from updating immediately when interacting with it.
        if (mRunning) {
            mController.updateView();
        }
    }
}
