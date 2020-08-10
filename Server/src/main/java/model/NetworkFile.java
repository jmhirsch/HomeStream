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

    public NetworkFile(File file, String pathFromRoot, NetworkFilesystem root, long hash) {
        super(file, FileType.FILE, root, hash);
        this.pathFromRoot = pathFromRoot + "/" + getName();
        // enables retrieving just the name or just the extension
        int extensionIndex = file.getName().lastIndexOf('.');
        this.nameWithoutExtension = file.getName().substring(0, extensionIndex);
        this.extension = file.getName().substring(extensionIndex);
    }


    // Returns a JSONObject of the file containing its name and its hash
    public JSONObject getJSONFile(){
        JSONObject jsonFile = new JSONObject();
        jsonFile.put("name", getName());
        jsonFile.put("hash", getHash());
        return jsonFile;
    }

    public String getNameWithoutExtension() {
        return this.nameWithoutExtension;
    }

    public String getExtension(){
        return this.extension;
    }
}
