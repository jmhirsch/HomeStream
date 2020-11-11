package interfaces;

import org.json.JSONObject;

public interface Data {
    int id = 0;
    String title = "";


    int getId();
    JSONObject toJSONObject();
}
