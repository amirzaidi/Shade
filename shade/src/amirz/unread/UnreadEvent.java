package amirz.unread;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class UnreadEvent {
    private List<String> mText;
    private View.OnClickListener mOnClick;

    UnreadEvent() {
        mText = new ArrayList<>();
    }

    UnreadEvent(UnreadEvent event) {
        mText = event.mText;
        mOnClick = event.mOnClick;
    }

    public List<String> getText() {
        return mText;
    }

    public View.OnClickListener getOnClickListener() {
        return mOnClick;
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        mOnClick = onClickListener;
    }
}
