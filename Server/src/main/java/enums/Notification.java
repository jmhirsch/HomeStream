package enums;

public enum Notification {
    FILE_UPDATED(0),
    FOLDER_UPDATED(1),
    REFRESH_CALLED(2);

    private final int tag;
     Notification(int tag){
        this.tag = tag;
    }

}
