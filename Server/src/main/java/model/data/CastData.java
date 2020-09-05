package model.data;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CastData extends Data {
    private final String name;
    private final String character;
    private final int order;
    private final List<MovieData> movieDataList;
    
    public CastData(String name, String character, int order, int id, String image) {
        super(id, image);
        this.name = name;
        this.character = character;
        this.order = order;
        movieDataList = new ArrayList<>();
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject object = super.toJSONObject();
        object.put("name", name);
        object.put("character", character);
        object.put("order", order);
        return object;
    }

    public int getOrder(){
        return order;
    }
}
