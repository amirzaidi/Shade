package amirz.shade.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.searchlauncher.SmartspaceQsbWidget;

import java.util.List;

import amirz.unread.UnreadEvent;
import amirz.unread.UnreadSession;

public class SmartUnreadQsbWidget extends SmartspaceQsbWidget
        implements UnreadSession.OnUpdateListener {
    private final UnreadSession mUnread;

    private View mSmartspaceView;
    private View mUnreadView;
    private TextView mUnreadTitle;
    private TextView mUnreadSubtitle;

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
        mSmartspaceView = findViewById(R.id.smartspace_view);
        mUnreadView = findViewById(R.id.unread_view);
        mUnreadTitle = mUnreadView.findViewById(R.id.shadespace_title);
        mUnreadSubtitle = mUnreadView.findViewById(R.id.shadespace_subtitle);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mUnread.addUpdateListener(this);
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
