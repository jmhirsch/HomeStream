package enums;

public enum ContextType {
    FILE_CONTEXT(0),
    FOLDER_CONTEXT(1),
    REFRESH_CONTEXT(2),
    GET_DATA_CONTEXT(3),
    PATCH_FILE_FOLDER(4),
    STREAMING_START_CONTEXT(5);

    private final int tag;
    ContextType(int tag){
        this.tag = tag;
    }
}
