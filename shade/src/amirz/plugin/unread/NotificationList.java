package amirz.plugin.unread;

import android.service.notification.StatusBarNotification;

import com.android.launcher3.notification.NotificationKeyData;
import com.android.launcher3.notification.NotificationListener;
import com.android.launcher3.util.PackageUserKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class NotificationList implements NotificationListener.NotificationsChangedListener {
    private final Runnable mOnNotificationsChanged;
    private final List<NotificationKeyData> mNotifications = new ArrayList<>();

    NotificationList(Runnable onNotificationsChanged) {
        mOnNotificationsChanged = onNotificationsChanged;
    }

    boolean hasNotifications() {
        return !mNotifications.isEmpty();
    }

    List<NotificationKeyData> getKeys() {
        return Collections.unmodifiableList(mNotifications);
    }

    @Override
    public void onNotificationPosted(PackageUserKey postedPackageUserKey,
                                     NotificationKeyData notificationKey,
                                     boolean shouldBeFilteredOut) {
        if (!shouldBeFilteredOut) {
            mNotifications.remove(notificationKey);
            mNotifications.add(notificationKey);
            mOnNotificationsChanged.run();
        }
    }

    @Override
    public void onNotificationRemoved(PackageUserKey removedPackageUserKey,
                                      NotificationKeyData notificationKey) {
        if (mNotifications.remove(notificationKey)) {
            mOnNotificationsChanged.run();
        }
    }

    @Override
    public void onNotificationFullRefresh(List<StatusBarNotification> activeNotifications) {
        mNotifications.clear();
        for (int i = activeNotifications.size() - 1; i >= 0; i--) {
            mNotifications.add(NotificationKeyData.fromNotification(activeNotifications.get(i)));
        }
        mOnNotificationsChanged.run();
    }
}
