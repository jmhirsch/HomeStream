package Model;

import Enums.FileType;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

public class Folder extends Filesystem {

    private static final String[] extensionlist = {".mp4", ".m4a", ".m4v", ".f4v", ".fa4", ".m4b", ".m4r", ".f4b", ".mov", ".3gp",
            ".3gp2", ".3g2", ".3gpp", ".3gpp2", ".ogg", ".oga", ".ogv", ".ogx", ".wmv", ".wma",
            ".webm", ".flv", ".avi", ".mpg", ".mkv"};

    private ArrayList<Folder> folders = new ArrayList<>();
    private ArrayList<CFile> files = new ArrayList<>();

    private String pathFromRoot;

    public Folder(File file, String pathFromRoot, boolean isRoot) {
        super(file, FileType.FOLDER);
        if (isRoot){
            this.pathFromRoot = "/";
        }else {
            this.pathFromRoot = pathFromRoot + getName()+ "/";
        }
        System.out.println(pathFromRoot);

        File [] subfolders = file.listFiles(File::isDirectory);
        File [] files = file.listFiles(File::isFile);

        if (subfolders != null) {
            for (File subfolder: subfolders){
                folders.add(new Folder(subfolder, this.pathFromRoot, false));
            }
        }

        if (files != null) {
            for (File subfile: files){
                if (!subfile.getName().startsWith(".") && isMovieOrSubtitle(subfile.getName())) {
                    this.files.add(new CFile(subfile));
                }
            }
        }

        Collections.sort(folders);
        Collections.sort(this.files);
    }

    public void listAllFolders(){
        super.printName();
        for (Folder folder: folders){
            folder.listAllFolders();
        }
    }

    private ArrayList<String> getTopLevelFolderNames(){
        ArrayList<String> folderNames = new ArrayList<>();

        for (Folder folder: folders){
            folderNames.add(folder.getFile().getName());
        }
        return folderNames;
    }

    private ArrayList<String> getFileNames(){

        ArrayList<String> fileNames = new ArrayList<>();

        for (CFile file: files){
            fileNames.add(file.getFile().getName());
        }
        return fileNames;
    }

    public JSONObject getJSONTopLevelFolders(){
        JSONObject items = new JSONObject();
        items.put("subfolders", getTopLevelFolderNames());
        System.out.println("got items");
        return items;
    }

    public JSONObject getJSONFiles(){
        JSONObject items = new JSONObject();
        items.put("files", getFileNames());
        return items;
    }

    public ArrayList<Folder> getFolders() {
        return folders;
    }

    public void listAllFiles(){
        for (CFile file: files){
            file.printName();
        }
    }

    public String getPathFromRoot(){
        return pathFromRoot;
    }

    private boolean isMovieOrSubtitle(String str){
        if (str.endsWith(".srt")){
            return true;
        }
        for (String extension: extensionlist){
            if (str.endsWith(extension)){
                return true;
            }
        }
        return false;
    }
}
