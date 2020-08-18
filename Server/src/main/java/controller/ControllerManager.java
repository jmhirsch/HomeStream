package controller;

import interfaces.NotificationListener;

public abstract class ControllerManager implements NotificationListener {

    public abstract void requestData();
}
