package amirz.shade.shadespace;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.CalendarContract;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;

import com.android.launcher3.Launcher;
import com.android.launcher3.notification.NotificationInfo;

class ShadespaceController {
    private static final int MULTI_CLICK_DELAY = 300;

    private final DoubleLineView mView;
    private final MediaListener mMedia;
    private final MultiClickListener mTaps;
    private final NotificationRanker mRanker;

    ShadespaceController(DoubleLineView view, MediaListener media, NotificationRanker ranker) {
        mView = view;

        mMedia = media;
        mTaps = new MultiClickListener(MULTI_CLICK_DELAY);
        mTaps.setListeners(mMedia::toggle, mMedia::next, mMedia::previous);

        mRanker = ranker;
    }

    void reload() {
        if (mMedia.isTracking()) {
            mView.setTopText(mMedia.getTitle());
            CharSequence app = getApp(mMedia.getPackage());
            if (TextUtils.isEmpty(mMedia.getArtist())) {
                mView.setBottomText(app);
            } else if (TextUtils.isEmpty(mMedia.getAlbum())
                    || mMedia.getTitle().equals(mMedia.getAlbum())) {
                mView.setBottomText(mMedia.getArtist());
            } else {
                mView.setBottomTextSplit(mMedia.getArtist(), mMedia.getAlbum());
            }
            mView.setOnClickListener(mTaps);
        } else {
            // Default values
            mView.setTopText(DateUtils.formatDateTime(mView.getContext(), System.currentTimeMillis(),
                    DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE));
            mView.setOnClickListener(this::openCalendar);

            NotificationRanker.RankedNotification ranked = mRanker.getBestNotification();
            if (ranked == null) {
                mView.resetBottomText();
            } else {
                NotificationInfo notif = new NotificationInfo(mView.getContext(), ranked.sbn);
                CharSequence app = getApp(notif.packageUserKey.mPackageName);
                String title = notif.title == null
                        ? ""
                        : notif.title.toString();
                String text = notif.text == null
                        ? ""
                        : notif.text.toString().trim().split("\n")[0];

                if (ranked.important) {
                    if (TextUtils.isEmpty(text)) {
                        mView.setTopText(title);
                        mView.setBottomText(app);
                    } else {
                        mView.setTopText(text);
                        setBottom(title, app);
                    }
                    mView.setOnClickListener(notif);
                } else if (TextUtils.isEmpty(text)) {
                    setBottom(title, app);
                } else {
                    mView.setBottomTextSplit(title, text);
                }
            }
        }
    }

    private void setBottom(String title, CharSequence app) {
        if (title.contains(": ") || title.contains(" - ") || title.equals(app.toString())) {
            mView.setBottomText(title);
        } else {
            mView.setBottomTextSplit(title, app);
        }
    }

    private CharSequence getApp(String name) {
        PackageManager pm = mView.getContext().getPackageManager();
        try {
            return pm.getApplicationLabel(
                    pm.getApplicationInfo(name, PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return name;
    }

    private void openCalendar(View v) {
        Uri.Builder timeUri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time");
        ContentUris.appendId(timeUri, System.currentTimeMillis());
        Intent addFlags = new Intent(Intent.ACTION_VIEW)
                .setData(timeUri.build())
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        Launcher.getLauncher(mView.getContext()).startActivitySafely(v, addFlags, null);
    }

    interface DoubleLineView {
        void setTopText(CharSequence s);

        void resetBottomText();

        void setBottomText(CharSequence s);

        void setBottomTextSplit(CharSequence s1, CharSequence s2);

        void setOnClickListener(View.OnClickListener l);

        Context getContext();
    }
}
