package enums;

public enum DataType {
    MOVIE("movie"),
    TV_SHOW("tv show"),
    SUBTITLE("subtitle"),
    NULL("null");

    private final String tag;
    DataType(String tag){
        this.tag = tag;
    }
}
