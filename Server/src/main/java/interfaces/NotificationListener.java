package interfaces;

import enums.Notification;

public interface NotificationListener {
     void NotificationReceived(Notification notification, Object obj);
}
