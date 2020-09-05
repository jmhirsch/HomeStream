package model.data;

import org.json.JSONObject;

public abstract class Data {
    protected final int id;
    protected final String image;

    public Data(int id, String image){
        this.id = id;
        this.image = image;
    }

    public JSONObject toJSONObject(){
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("image", image);
        return object;
    }

    public int getId(){
        return this.id;
    }

}
