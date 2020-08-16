package interfaces;

import enums.Notification;

public interface NotificationListener {
    public void NotificationReceived(Notification notification, Object obj);
}
