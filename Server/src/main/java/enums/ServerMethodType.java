package enums;

public enum ServerMethodType {
    GET("GET"),
    POST("POST"),
    PATCH("PATCH"),
    PUT("PUT"),
    DELETE("DELETE");

    private final String type;

    ServerMethodType(String type){
        this.type = type;
    }
}
