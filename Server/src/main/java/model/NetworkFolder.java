package model;

import enums.FileType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/*
Defines a network folder object, which will be sent through the server
Folder objects should be given an array of extension for files to consider adding.
    If none or an empty array is passed, all files will be added
Folder objects can be given an array of folder names to ignore.
    If nore or an empty array is passed, all folders will be added
Folder Can be built using JSON to represent itself and its children
JSON can either return all items contained in the folder, or just the top level folders, or just top level files
Folder is automatically sorted alphabetically. Override compareTo() to implement custom sorting
files Folders beginning with "." are ignored. Currently, this cannot be changed
 */
public class NetworkFolder extends NetworkFilesystem {

    private final String[] extensionArray;
    private final String [] folderNamesToIgnore;

    private List<NetworkFolder> folders = new ArrayList<>();
    private List<NetworkFile> files = new ArrayList<>();

    //Public constructor for root folder
    public NetworkFolder(File file, String[] extensionArray, String [] folderNamesToIgnore) {
        super(file, FileType.FOLDER, 0);
        this.pathFromRoot = "";
        this.extensionArray = extensionArray;
        this.folderNamesToIgnore = folderNamesToIgnore;
        setupChildrenAndSort(file);
        this.pathFromRoot = "/";
    }

    // Public constructor for all filetypes and all folders
    public NetworkFolder(File file){
        this(file, new String[0], new String[0]);
    }

    public NetworkFolder(File file, String [] extensionArray){
        this(file, extensionArray, new String[0]);
    }

    //Private constructor for subfolders
    private NetworkFolder(File file, String pathFromRoot, NetworkFilesystem root, long hash, String [] extensionArray, String [] folderNamesToIgnore){
        super(file, FileType.FOLDER, root, hash);
        this.folderNamesToIgnore = folderNamesToIgnore;
        this.extensionArray = extensionArray;
        this.pathFromRoot =  pathFromRoot + "/" + getName();
        setupChildrenAndSort(file);

    }

    private void setupChildrenAndSort(File file) {
        addSubfolders(file);
        addFiles(file);
        sort();
    }

    //Sorts according to implementation of compareTo(). Default is alphabetical
    private void sort() {
        Collections.sort(folders);
        Collections.sort(files);
    }

    //Adds all the files of the current folder as children, if their file extension is in the extensionArray
    // If no array was provided, or if the array is empty, all files will be added
    private void addFiles(File file) {
        File [] files = file.listFiles(File::isFile);
        if (files != null) {
            for (File subfile: files){
                if (!subfile.getName().startsWith(".") && fileExtensionExistsInList(subfile.getName())) {
                    this.files.add(new NetworkFile(subfile, this.pathFromRoot, this.getRoot(), this.getHash()));
                }
            }
        }
    }

    // Adds all the subfolders
    private void addSubfolders(File folder) {
        File [] subfolders = folder.listFiles(File::isDirectory);
        if (subfolders != null) {
            for (File subfolder: subfolders){
                if (folderShouldBeIgnored(subfolder.getName()) ||
                subfolder.getName().startsWith(".")){
                    continue;
                }
                folders.add(new NetworkFolder(subfolder, this.pathFromRoot, this.getRoot(), this.getHash(), extensionArray, folderNamesToIgnore));
            }
        }
    }

    //Returns an array containing the names of all the immediate subfolders of the current folder
    // (Non recursive items)
    private ArrayList<String> getTopLevelFolderNames(){
        ArrayList<String> folderNames = new ArrayList<>();
        for (NetworkFolder folder: folders){
            folderNames.add(folder.getFile().getName());
        }
        return folderNames;
    }

    //Returns an array contianing the names of all the immediate files of the current folder
    // (Non recursive items)
    private ArrayList<String> getFileNames(){
        ArrayList<String> fileNames = new ArrayList<>();
        for (NetworkFile file: files){
            fileNames.add(file.getFile().getName());
        }
        return fileNames;
    }

    //Returns a JSON object containing an array the top level folder names of the current folder, as defined
    // in getTopLevelFolderNames
    //Key is "subfolders",
    public JSONObject getJSONTopLevelFolders(){
        JSONObject items = new JSONObject();
        items.put("subfolders", getTopLevelFolderNames());
        return items;
    }

    /*
        Returns a JSON Object containing information about the current folder,
        an array of its files, and an array of objects of its subfolders
        Recursive call, ensure this happens in the background if there is a large hiearchy
        Keys:
            - name: folder name
            - hash: folder hash
            - path: path from root folder
            - files: array of JSONFiles as defined in getJSONFiles()
            - subfolders: array of JSONObjects recursively containing subfolders of current folder
     */
    public JSONObject getJSONItems(){
        JSONObject items = new JSONObject();
        items.put("name", getName());
        items.put("hash", getHash());
        items.put("path", getPathFromRoot());
        items.put("files", getJSONFiles());
        JSONArray subfolders = new JSONArray();

        for (NetworkFolder subfolder: folders){
            subfolders.put(subfolder.getJSONItems());
        }

        items.put("subfolders", subfolders);
        return items;
    }

    // Returns a JSON array containing all the JSON data of files in current folder
    public JSONArray getJSONFiles(){
        JSONArray items = new JSONArray();
        for (NetworkFile file: files){
            items.put(file.getJSONFile());
        }
        return items;
    }

    // returns all the subfolder objects (unmodifiable)
    public Collection<NetworkFolder> getFolders() {
        return Collections.unmodifiableCollection(folders);
    }

    //Returns all the subfile objects (unmodifiable)
    public Collection<NetworkFile> getFiles(){
        return Collections.unmodifiableCollection(files);
    }

    //prints all files in current folder
    public void listAllFiles(){
        for (NetworkFile file: files){
            file.printName();
        }
    }

    //Prints all folders to console recursively
    public void listAllFolders(){
        super.printName();
        for (NetworkFolder folder: folders){
            folder.listAllFolders();
        }
    }

    // Compares folder name to array of folder names to ignore
    // Returns false if no list was passed
    // Returns true if folder should be ignored, false otherwise
    private boolean folderShouldBeIgnored(String folderName){
        if (folderNamesToIgnore.length == 0){
            return false;
        }
        for (String folderNameToIgnore: folderNamesToIgnore){
            if (folderName.equals(folderNameToIgnore)){
                return true;
            }
        }
        return true;
    }

    //Compares file extension to array of extensions to include
    // Returns true if no array is passed, or if filename matches extension list
    // returns false otherwise
    private boolean fileExtensionExistsInList(String filename){
        if (extensionArray.length == 0){
            return true;
        }
        for (String extension: extensionArray){
            if (filename.endsWith(extension)){
                return true;
            }
        }
        return false;
    }
}
