package com.android.launcher3.notification;

import android.service.notification.StatusBarNotification;

import com.android.launcher3.util.PackageUserKey;

import java.util.HashSet;
import java.util.List;

public class NotificationListenerProxy
        extends HashSet<NotificationListener.NotificationsChangedListener>
        implements NotificationListener.NotificationsChangedListener {
    public static final NotificationListenerProxy INSTANCE = new NotificationListenerProxy();

    // Prevent initialization.
    private NotificationListenerProxy() {
    }

    @Override
    public void onNotificationPosted(PackageUserKey postedPackageUserKey,
                                     NotificationKeyData notificationKey,
                                     boolean shouldBeFilteredOut) {
        for (NotificationListener.NotificationsChangedListener listener : this) {
            listener.onNotificationPosted(postedPackageUserKey, notificationKey, shouldBeFilteredOut);
        }
    }

    @Override
    public void onNotificationRemoved(PackageUserKey removedPackageUserKey,
                                      NotificationKeyData notificationKey) {
        for (NotificationListener.NotificationsChangedListener listener : this) {
            listener.onNotificationRemoved(removedPackageUserKey, notificationKey);
        }
    }

    @Override
    public void onNotificationFullRefresh(List<StatusBarNotification> activeNotifications) {
        for (NotificationListener.NotificationsChangedListener listener : this) {
            listener.onNotificationFullRefresh(activeNotifications);
        }
    }
}
