package model.requests;

import com.sun.net.httpserver.HttpExchange;
import model.requests.NetworkRequest;
import org.json.JSONObject;

public class FileUpdateRequest<JSONObject> extends NetworkRequest<JSONObject> {
    public FileUpdateRequest(long requestNum, HttpExchange exchange, String path) {
        super(requestNum, exchange, path);
    }

    @Override
    public void completeRequest(JSONObject data) {

    }
}
