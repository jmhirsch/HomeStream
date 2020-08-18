package model.requests;

import com.sun.net.httpserver.HttpHandler;

public abstract class Handler implements HttpHandler {
    private final String path;

    public Handler(String path) {
        this.path = path;
    }

    public String getPath(){
        return path;
    }
}
