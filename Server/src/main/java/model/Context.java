package model;

import enums.ContextType;

public class Context {
    private final String path;
    private final ContextType type;

    public Context(String path, ContextType type){
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public ContextType getType() {
        return type;
    }


}
