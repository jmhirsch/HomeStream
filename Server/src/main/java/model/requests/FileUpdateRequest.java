package model.requests;

import com.sun.net.httpserver.HttpExchange;

public class FileUpdateRequest<JSONObject> extends NetworkRequest<JSONObject> {
    public FileUpdateRequest(long requestNum, HttpExchange exchange, String path) {
        super(requestNum, exchange, path);
    }

    @Override
    public JSONObject completeRequest(JSONObject object) {
        return null;
    }

}
