package enums;

import interfaces.NotificationType;

public enum Notification implements NotificationType {
    PATCH_WITH_HASH(1),
    REFRESH_CALLED(2),
    FILE_STREAMING_REQUESTED(3),
    GET_CALLED(4);

    private final int tag;
     Notification(int tag){
        this.tag = tag;
    }
}
