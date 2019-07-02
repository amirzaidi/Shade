package com.android.launcher3;

import android.app.Notification;
import android.service.notification.StatusBarNotification;

import com.android.launcher3.notification.NotificationKeyData;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.util.PackageUserKey;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LauncherNotifications implements NotificationListener.NotificationsChangedListenerExt {
    private static LauncherNotifications sInstance;

    public static synchronized LauncherNotifications getInstance() {
        if (sInstance == null) {
            sInstance = new LauncherNotifications();
        }
        return sInstance;
    }

    private final Set<NotificationListener.NotificationsChangedListener> mListeners = new HashSet<>();
    private final Set<NotificationListener.NotificationsChangedListener> mUnfiltered = new HashSet<>();

    public void addListener(NotificationListener.NotificationsChangedListener listener) {
        mListeners.add(listener);
    }

    public void addUnfilteredListener(NotificationListener.NotificationsChangedListener listener) {
        mUnfiltered.add(listener);
    }

    @Override
    public void onNotificationPosted(PackageUserKey postedPackageUserKey, NotificationKeyData notificationKey, boolean shouldBeFilteredOut) {
        for (NotificationListener.NotificationsChangedListener listener : mListeners) {
            listener.onNotificationPosted(postedPackageUserKey, notificationKey, shouldBeFilteredOut);
        }
        for (NotificationListener.NotificationsChangedListener listener : mUnfiltered) {
            listener.onNotificationPosted(postedPackageUserKey, notificationKey, false);
        }
    }

    @Override
    public void onNotificationRemoved(PackageUserKey removedPackageUserKey, NotificationKeyData notificationKey) {
        for (NotificationListener.NotificationsChangedListener listener : mListeners) {
            listener.onNotificationRemoved(removedPackageUserKey, notificationKey);
        }
        for (NotificationListener.NotificationsChangedListener listener : mUnfiltered) {
            listener.onNotificationRemoved(removedPackageUserKey, notificationKey);
        }
    }

    @Override
    public void onNotificationFullRefresh(List<StatusBarNotification> activeNotifications) {
        for (NotificationListener.NotificationsChangedListener listener : mListeners) {
            listener.onNotificationFullRefresh(activeNotifications);
        }
    }

    @Override
    public void onNotificationUnfilteredRefresh(List<StatusBarNotification> unfilteredNotifications) {
        for (NotificationListener.NotificationsChangedListener listener : mUnfiltered) {
            listener.onNotificationFullRefresh(unfilteredNotifications);
        }
    }
}
