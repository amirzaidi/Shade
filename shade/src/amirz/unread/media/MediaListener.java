package amirz.unread.media;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Handler;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.android.launcher3.notification.NotificationListener;

import java.util.Collections;
import java.util.List;

import amirz.unread.notifications.NotificationList;
import amirz.unread.notifications.PendingIntentSender;

public class MediaListener extends MediaController.Callback
        implements MediaSessionManager.OnActiveSessionsChangedListener, View.OnClickListener {
    private static final String TAG = "MediaListener";

    private static final int MULTI_CLICK_DELAY = 200;

    private final ComponentName mComponent;
    private final MediaSessionManager mManager;
    private final Handler mWorkerHandler;
    private final Runnable mOnChange;
    private final MultiClickListener mTaps;
    private final NotificationList mNotifs;
    private final PendingIntentSender mSender;

    private List<MediaController> mControllers = Collections.emptyList();
    private MediaController mTracking;

    public MediaListener(Context context, Handler workerHandler, Runnable onChange,
                         NotificationList notifs, PendingIntentSender sender) {
        mComponent = new ComponentName(context, NotificationListener.class);
        mManager = context.getSystemService(MediaSessionManager.class);
        mWorkerHandler = workerHandler;
        mOnChange = onChange;
        mTaps = new MultiClickListener(MULTI_CLICK_DELAY);
        mTaps.setListeners(this::toggle, this::next, this::previous);
        mNotifs = notifs;
        mSender = sender;
    }

    public void onCreate() {
        try {
            mManager.addOnActiveSessionsChangedListener(this, mComponent, mWorkerHandler);
        } catch (SecurityException e) {
            if (e.getMessage() != null) {
                Log.d(TAG, e.getMessage());
            }
        }
        onActiveSessionsChanged(null); // Bind all current controllers.
    }

    public void onDestroy() {
        mManager.removeOnActiveSessionsChangedListener(this);
        onActiveSessionsChanged(Collections.emptyList()); // Unbind all previous controllers.
    }

    public boolean isTracking() {
        return mTracking != null;
    }

    public boolean isPausedOrPlaying() {
        if (!isTracking()) {
            return false;
        }

        return isPausedOrPlaying(mTracking) && hasNotification(mTracking);
    }

    private boolean hasNotification(MediaController mc) {
        return getNotification(mc) != null;
    }

    private StatusBarNotification getNotification(MediaController mc) {
        for (StatusBarNotification sbn : mNotifs.getMap().keySet()) {
            if (mc.getPackageName().equals(sbn.getPackageName())) {
                if (sbn.getNotification().extras
                        .getParcelable(Notification.EXTRA_MEDIA_SESSION) != null) {
                    return sbn;
                }
            }
        }
        Log.d(TAG, "MediaController has no notification");
        return null;
    }

    public CharSequence getTitle() {
        MediaMetadata md = mTracking.getMetadata();
        return md == null ? null : md.getText(MediaMetadata.METADATA_KEY_TITLE);
    }

    public CharSequence getArtist() {
        MediaMetadata md = mTracking.getMetadata();
        return md == null ? null : md.getText(MediaMetadata.METADATA_KEY_ARTIST);
    }

    public CharSequence getAlbum() {
        MediaMetadata md = mTracking.getMetadata();
        return md == null ? null : md.getText(MediaMetadata.METADATA_KEY_ALBUM);
    }

    public String getPackage() {
        return mTracking.getPackageName();
    }

    private void updateControllers(List<MediaController> controllers) {
        for (MediaController mc : mControllers) {
            mc.unregisterCallback(this);
        }
        for (MediaController mc : controllers) {
            mc.registerCallback(this, mWorkerHandler);
        }
        mControllers = controllers;
    }

    @Override
    public void onActiveSessionsChanged(List<MediaController> controllers) {
        if (controllers == null) {
            try {
                controllers = mManager.getActiveSessions(mComponent);
            } catch (SecurityException e) {
                controllers = Collections.emptyList();
                if (e.getMessage() != null) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }
        updateControllers(controllers);

        // If the current controller is not paused or playing, stop tracking it.
        if (mTracking != null
                && (!controllers.contains(mTracking) || !isPausedOrPlaying(mTracking))) {
            mTracking = null;
        }

        for (MediaController mc : controllers) {
            // Either we are not tracking a controller and this one is valid,
            // or this one is playing while the one we track is not.
            if ((mTracking == null && isPausedOrPlaying(mc))
                    || (mTracking != null && isPlaying(mc) && !isPlaying(mTracking))) {
                mTracking = mc;
            }
        }

        Log.d(TAG, "onActiveSessionsChanged mTracking=" + (mTracking != null));
        mOnChange.run();
    }

    private void pressButton(int keyCode) {
        if (mTracking != null) {
            mTracking.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            mTracking.dispatchMediaButtonEvent(new KeyEvent(KeyEvent.ACTION_UP, keyCode));
        }
    }

    @Override
    public void onClick(View v) {
        mTaps.onClick(v);
    }

    public boolean open(View v) {
        if (mTracking != null) {
            Log.d(TAG, "Open");
            StatusBarNotification sbn = getNotification(mTracking);
            if (sbn != null) {
                PendingIntent pi = sbn.getNotification().contentIntent;
                if (pi != null) {
                    mSender.onClickNotification(pi).onClick(v);
                    return true;
                }
            }
        }
        return false;
    }

    private void toggle(View v, boolean finalClick) {
        if (finalClick) {
            Log.d(TAG, "Toggle");
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        }
    }

    private void next(View v, boolean finalClick) {
        if (finalClick) {
            Log.d(TAG, "Next");
            pressButton(KeyEvent.KEYCODE_MEDIA_NEXT);
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY);
        }
    }

    private void previous(View v, boolean finalClick) {
        if (finalClick) {
            Log.d(TAG, "Previous");
            pressButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY);
        }
    }

    private boolean isPlaying(MediaController mc) {
        if (!hasTitle(mc) || mc.getPlaybackState() == null) {
            return false;
        }
        int state = mc.getPlaybackState().getState();
        return state == PlaybackState.STATE_PLAYING;
    }

    private boolean isPausedOrPlaying(MediaController mc) {
        if (!hasTitle(mc) || mc.getPlaybackState() == null) {
            return false;
        }
        int state = mc.getPlaybackState().getState();
        return state == PlaybackState.STATE_PAUSED
                || state == PlaybackState.STATE_PLAYING;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean hasTitle(MediaController mc) {
        return mc != null && mc.getMetadata() != null
                && !TextUtils.isEmpty(mc.getMetadata().getText(MediaMetadata.METADATA_KEY_TITLE));
    }

    /**
     * Events that refresh the current handler.
     */
    public void onPlaybackStateChanged(PlaybackState state) {
        super.onPlaybackStateChanged(state);
        onActiveSessionsChanged(null);
    }

    public void onMetadataChanged(MediaMetadata metadata) {
        super.onMetadataChanged(metadata);
        onActiveSessionsChanged(null);
    }
}
