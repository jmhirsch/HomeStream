package Model;

import Enums.FileType;


import java.util.ArrayList;
import java.util.HashMap;

public class Folder extends Filesystem {
    private HashMap<FileType, ArrayList<? extends Filesystem>> filesystemArray;

    public Folder(String name, String pathFromRoot, ArrayList<Folder> folders, ArrayList<File> files) {
        super(name, pathFromRoot);
        filesystemArray = new HashMap<>();
        filesystemArray.put(FileType.FILE, files);
        filesystemArray.put(FileType.FOLDER, folders;
    }

    public ArrayList<Folder> getFolders(){
        return ((ArrayList<Folder>) filesystemArray.get(FileType.FOLDER));
    }

    public ArrayList<File> getFiles() {
        return ((ArrayList<File>) filesystemArray.get(FileType.FILE));
    }
}
