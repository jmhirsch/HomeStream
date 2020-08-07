package model;

import controller.Controller;
import enums.FileType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Folder extends Filesystem {

    private static final String[] extensionlist = {".mp4", ".m4a", ".m4v", ".f4v", ".fa4", ".m4b", ".m4r", ".f4b", ".mov", ".3gp",
            ".3gp2", ".3g2", ".3gpp", ".3gpp2", ".ogg", ".oga", ".ogv", ".ogx", ".wmv", ".wma",
            ".webm", ".flv", ".avi", ".mpg", ".mkv", ".ts"};

    private List<Folder> folders = new ArrayList<>();
    private List<CFile> files = new ArrayList<>();

    public Folder(File file) {
        super(file, FileType.FOLDER);
        this.pathFromRoot = "";

        setup(file);
            this.pathFromRoot = "/";
    }

    private Folder(File file, String pathFromRoot, Filesystem root){
        super(file, FileType.FOLDER, root);

        this.pathFromRoot =  pathFromRoot + "/" + getName();
        setup(file);

    }

    private void setup(File file) {
        addSubfolders(file);
        addFiles(file);
        sort();
    }

    private void sort() {
        Collections.sort(folders);
        Collections.sort(files);
    }

    private void addFiles(File file) {
        File [] files = file.listFiles(File::isFile);
        if (files != null) {
            for (File subfile: files){
                if (!subfile.getName().startsWith(".") && isMovieOrSubtitle(subfile.getName())) {
                    this.files.add(new CFile(subfile, this.pathFromRoot, this.getRoot()));
                }
            }
        }
    }

    private void addSubfolders(File folder) {
        File [] subfolders = folder.listFiles(File::isDirectory);
        if (subfolders != null) {
            for (File subfolder: subfolders){
                if (subfolder.getName().contains(Controller.CACHE_FOLDER_IGNORE_STR) ||
                subfolder.getName().startsWith(".")){
                    continue;
                }
                folders.add(new Folder(subfolder, this.pathFromRoot, this.getRoot()));
            }
        }
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




        JSONObject itemsV2 = new JSONObject();
        return items;
    }

    public JSONObject getJSONItems(){
        JSONObject items = new JSONObject();
        items.put("name", getName());
        items.put("path", getPathFromRoot());
        items.put("files", getFileNames());
        JSONArray subfolders = new JSONArray();

        for (Folder subfolder: folders){
            subfolders.put(subfolder.getJSONItems());
        }

        items.put("subfolders", subfolders);
        return items;
    }



    public JSONArray getJSONFiles(){
        JSONArray items = new JSONArray();
        items.put(getFileNames());
        return items;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public List<CFile> getFiles(){
        return files;
    }

    public void listAllFiles(){
        for (CFile file: files){
            file.printName();
        }
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
