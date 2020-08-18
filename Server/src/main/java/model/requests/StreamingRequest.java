package model.requests;

import com.sun.net.httpserver.HttpExchange;

public class StreamingRequest<JSONObject> extends NetworkRequest<JSONObject> {
    public StreamingRequest(long requestNum, HttpExchange exchange, String path) {
        super(requestNum, exchange, path);
    }

    @Override
    public JSONObject completeRequest(JSONObject object) {
        return object;
    }
}