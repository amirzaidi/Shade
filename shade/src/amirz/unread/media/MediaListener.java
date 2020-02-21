package amirz.unread.media;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.android.launcher3.notification.NotificationListener;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import amirz.unread.notifications.NotificationList;

public class MediaListener extends MediaController.Callback
        implements MediaSessionManager.OnActiveSessionsChangedListener {
    private static final String TAG = "MediaListener";

    private static final int MULTI_CLICK_DELAY = 300;

    private final ComponentName mComponent;
    private final MediaSessionManager mManager;
    private final Runnable mOnChange;
    private final MultiClickListener mTaps;
    private final Set<NotificationList.Notif> mNotifs;
    private List<MediaController> mControllers = Collections.emptyList();
    private MediaController mTracking;

    public MediaListener(Context context, Runnable onChange, Set<NotificationList.Notif> notifs) {
        mComponent = new ComponentName(context, NotificationListener.class);
        mManager = context.getSystemService(MediaSessionManager.class);
        mOnChange = onChange;
        mTaps = new MultiClickListener(MULTI_CLICK_DELAY);
        mTaps.setListeners(this::toggle, this::next, this::previous);
        mNotifs = notifs;
    }

    public void onResume() {
        try {
            mManager.addOnActiveSessionsChangedListener(this, mComponent);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        onActiveSessionsChanged(null); // Bind all current controllers.
    }

    public void onPause() {
        mManager.removeOnActiveSessionsChangedListener(this);
        onActiveSessionsChanged(Collections.emptyList()); // Unbind all previous controllers.
    }

    public boolean isTracking() {
        return mTracking != null;
    }

    public boolean isPausedOrPlaying() {
        return isTracking() && isPausedOrPlaying(mTracking);
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
            mc.registerCallback(this);
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
                e.printStackTrace();
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

    public void onClick() {
        mTaps.onClick();
    }

    private void toggle(boolean finalClick) {
        if (!finalClick) {
            Log.d(TAG, "Toggle");
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
        }
    }

    private void next(boolean finalClick) {
        if (finalClick) {
            Log.d(TAG, "Next");
            pressButton(KeyEvent.KEYCODE_MEDIA_NEXT);
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY);
        }
    }

    private void previous(boolean finalClick) {
        if (finalClick) {
            Log.d(TAG, "Previous");
            pressButton(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
            pressButton(KeyEvent.KEYCODE_MEDIA_PLAY);
        }
    }

    private boolean isPlaying(MediaController mc) {
        if (!hasNotification(mc) || !hasTitle(mc) || mc.getPlaybackState() == null) {
            return false;
        }
        int state = mc.getPlaybackState().getState();
        return state == PlaybackState.STATE_PLAYING;
    }

    private boolean isPausedOrPlaying(MediaController mc) {
        if (!hasNotification(mc) || !hasTitle(mc) || mc.getPlaybackState() == null) {
            return false;
        }
        int state = mc.getPlaybackState().getState();
        return state == PlaybackState.STATE_PAUSED
                || state == PlaybackState.STATE_PLAYING;
    }

    private boolean hasNotification(MediaController mc) {
        for (NotificationList.Notif notif : mNotifs) {
            StatusBarNotification sbn = notif.getSbn();
            if (mc.getPackageName().equals(sbn.getPackageName())) {
                return sbn.getNotification().extras
                        .getParcelable(Notification.EXTRA_MEDIA_SESSION) != null;
            }
        }
        Log.d(TAG, "MediaController has no notification");
        return false;
    }

    private boolean hasTitle(MediaController mc) {
        return mc.getMetadata() != null
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
