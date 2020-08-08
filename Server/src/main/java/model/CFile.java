package model;


import enums.FileType;
import org.json.JSONObject;

import java.io.File;

public class CFile extends Filesystem {


    private final String nameStripExtension;
    private final String extension;

    public CFile(File file, String pathFromRoot, Filesystem root, double hash) {
        super(file, FileType.FILE, root, hash);

        this.pathFromRoot = pathFromRoot + "/" + getName();

        int extensionIndex = file.getName().lastIndexOf('.');
        this.nameStripExtension = file.getName().substring(0, extensionIndex);
        this.extension = file.getName().substring(extensionIndex);
    }


    public JSONObject getJSONFile(){
        JSONObject jsonFile = new JSONObject();
        jsonFile.put("name", getName());
        jsonFile.put("hash", getHash());
        return jsonFile;
    }

    public String getNameStripExtension() {
        return this.nameStripExtension;
    }

    public String getExtension(){
        return this.extension;
    }
}
