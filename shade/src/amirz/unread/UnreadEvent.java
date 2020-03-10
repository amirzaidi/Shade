package amirz.unread;

import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class UnreadEvent {
    private final List<String> mText = new ArrayList<>();
    private View.OnClickListener mOnClick;

    // Prevent instantiation outside package.
    UnreadEvent() {
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
