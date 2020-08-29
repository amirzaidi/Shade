package amirz.shade.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.android.launcher3.R;
import com.android.searchlauncher.SmartspaceQsbWidget;

import java.util.List;

import amirz.unread.UnreadEvent;
import amirz.unread.UnreadSession;

public class SmartUnreadQsbWidget extends SmartspaceQsbWidget
        implements UnreadSession.OnUpdateListener {
    private final UnreadSession mUnread;

    private FrameLayout mSmartspaceView;
    private View mUnreadView;
    private DoubleShadowTextView mUnreadTitle;
    private DoubleShadowTextView mUnreadSubtitle;

    public SmartUnreadQsbWidget(Context context) {
        this(context,  null);
    }

    public SmartUnreadQsbWidget(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SmartUnreadQsbWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mUnread = UnreadSession.getInstance(context);
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mSmartspaceView = (FrameLayout) findViewById(R.id.smartspace_view);
        mUnreadView = findViewById(R.id.unread_view);
        mUnreadTitle = mUnreadView.findViewById(R.id.shadespace_title);
        mUnreadSubtitle = mUnreadView.findViewById(R.id.shadespace_subtitle);

        View vc = mSmartspaceView.getChildAt(0);
        if (vc instanceof ThemedSmartspaceHostView) {
            ThemedSmartspaceHostView hv =
                    (ThemedSmartspaceHostView) mSmartspaceView.getChildAt(0);
            hv.setSampleDoubleShadowTextView(mUnreadSubtitle);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mUnread.addUpdateListener(this);
        onUpdateAvailable();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mUnread.removeUpdateListener(this);
    }

    @Override
    public void onUpdateAvailable() {
        UnreadEvent event = mUnread.getEvent();
        mUnreadView.setOnClickListener(event.getOnClickListener());
        mUnreadView.setOnLongClickListener(event.getOnLongClickListener());
        List<String> text = event.getText();
        if (text != null && text.size() > 1) {
            mSmartspaceView.setVisibility(View.GONE);
            mUnreadView.setVisibility(View.VISIBLE);

            String top = text.get(0);
            String bottom = text.get(1);
            if (text.size() > 2) {
                bottom = getContext().getString(
                        R.string.shadespace_subtext_double, bottom, text.get(2));
            }

            mUnreadTitle.setText(top);
            mUnreadSubtitle.setText(bottom);
        } else {
            mUnreadView.setVisibility(View.GONE);
            mSmartspaceView.setVisibility(View.VISIBLE);
        }
    }
}
