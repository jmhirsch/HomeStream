package model;


import enums.FileType;
import org.json.JSONObject;

import java.io.File;
/*
Defines a network file object used to pass to a server
 */
public class NetworkFile extends NetworkFilesystem {
    private final String nameWithoutExtension;
    private final String extension;
    private int currentPlaybackPosition;

    public NetworkFile(File file, String pathFromRoot, NetworkFilesystem root, long hash) {
        super(file, FileType.FILE, root, hash);
        this.pathFromRoot = pathFromRoot + "/" + getName();
        // enables retrieving just the name or just the extension
        int extensionIndex = file.getName().lastIndexOf('.');
        this.nameWithoutExtension = file.getName().substring(0, extensionIndex);
        this.extension = file.getName().substring(extensionIndex);
        this.currentPlaybackPosition = 0;
    }


    // Returns a JSONObject of the file containing its name and its hash
    public JSONObject getJSONFile(){
        JSONObject jsonFile = super.getData();
        jsonFile.put("playbackPosition", currentPlaybackPosition);
        return jsonFile;
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
