package model.requests;

import com.sun.net.httpserver.HttpExchange;

public class NetworkRequest<JSONObject> extends Request<JSONObject> {
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

    public HttpExchange getExchange(){
        return exchange;
    }

    public  JSONObject completeRequest(JSONObject object){
        return object;
    }
}
