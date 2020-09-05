package model.network;


import enums.DataType;
import enums.FileType;
import model.data.Data;
import org.json.JSONObject;

import java.io.File;
/*
Defines a network file object used to pass to a server
 */
public class NetworkFile extends NetworkFilesystem {
    private final String nameWithoutExtension;
    private final String extension;
    private final DataType dataType;
    private int currentPlaybackPosition;
    private Data data;
    private int databasekey = -1;

    public NetworkFile(File file, String pathFromRoot, DataType dataType, NetworkFilesystem root, long hash) {
        super(file, FileType.FILE, root, hash);
        this.dataType = dataType;
        this.pathFromRoot = pathFromRoot + "/" + getName();
        // enables retrieving just the name or just the extension
        int extensionIndex = file.getName().lastIndexOf('.');
        this.nameWithoutExtension = file.getName().substring(0, extensionIndex);
        this.extension = file.getName().substring(extensionIndex);
        this.currentPlaybackPosition = 0;
    }


    // Returns a JSONObject of the file containing its name and its hash
    public JSONObject getData(){
        JSONObject jsonFile = super.getData();
        jsonFile.put("playbackPosition", currentPlaybackPosition);
        jsonFile.put("databaseKey", databasekey);
        jsonFile.put("type", dataType.toString());
        if (data != null) {
            jsonFile.put("data", data.toJSONObject());
        }
        return jsonFile;
    }

    public void setData(Data data){
        this.data = data;
        this.databasekey = data.getId();
    }

    public int getDatabasekey() {
        return databasekey;
    }

    public DataType getDataType(){
        return dataType;
    }

    public void setDatabasekey(int key){
        this.databasekey = key;
    }

    public String getNameWithoutExtension() {
        return this.nameWithoutExtension;
    }

    public String getExtension(){
        return this.extension;
    }

    public int getCurrentPlaybackPosition(){
        return currentPlaybackPosition;
    }

    public void setCurrentPlaybackPosition(int currentPlaybackPosition){
        this.currentPlaybackPosition = currentPlaybackPosition;
    }
}
