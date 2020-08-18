package model;

import enums.ServerMethodType;
import model.requests.Notification;

public record Context(String path, ServerMethodType type, Notification notification){}
