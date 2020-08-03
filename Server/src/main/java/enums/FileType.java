package enums;

public enum FileType {
    FILE(0),
    FOLDER(1);

    private final int tag;
     FileType(int tag){
        this.tag = tag;
    }
}
