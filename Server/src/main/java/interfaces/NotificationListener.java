package interfaces;

import model.requests.Notification;

public interface NotificationListener {
     void notificationReceived(Notification notification, Object obj, long id);
}
