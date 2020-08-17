package model.requests;

import com.sun.net.httpserver.HttpExchange;

public abstract class NetworkRequest<T> extends Request<T> {
    private final HttpExchange exchange;
    private final String path;
    public NetworkRequest(long requestNum, HttpExchange exchange, String path) {
        super(requestNum);
        this.exchange = exchange;
        this.path = path;
    }

    public String getPath(){
        return path;
    }

}
