package interfaces;

import enums.Notification;

public interface NotificationListener {
     void notificationReceived(Notification notification, Object obj, long id);
}
