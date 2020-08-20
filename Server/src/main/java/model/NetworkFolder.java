package model;

import com.sun.jna.platform.mac.XAttrUtil;
import enums.FileType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

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

    //private List<NetworkFolder> folders = new ArrayList<>();
    //private List<NetworkFile> files = new ArrayList<>();


    private Map<Long, NetworkFolder> folders = new TreeMap<>();
    private Map<Long, NetworkFile> files = new TreeMap<>();

    //Public constructor for root folder
    public NetworkFolder(File file, String[] extensionArray, String [] folderNamesToIgnore) {
        super(file, FileType.FOLDER, 0);
        this.pathFromRoot = "";
        this.extensionArray = extensionArray;
        this.folderNamesToIgnore = folderNamesToIgnore;
        setupChildrenAndSort(file);
        this.pathFromRoot = "/";
    }

    public NetworkFolder(File file, String [] extensionArray, String [] folderNamesToIgnore, boolean do_not_create_folders){
        super(file, FileType.FOLDER, 0);
        this.pathFromRoot = "/";
        this.extensionArray = extensionArray;
        this.folderNamesToIgnore = folderNamesToIgnore;
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

    public void addNetworkItem(NetworkFilesystem item){
        if (item instanceof NetworkFile file){
            files.put(file.getHash(), file);
            files = (Map<Long, NetworkFile>) sortByValue(files, true);
        }else if (item instanceof NetworkFolder folder){
            folders.put(folder.getHash(), folder);
            folders = (Map<Long, NetworkFolder>) sortByValue(folders, true);
        }
    }

    public void removeNetworkItem(NetworkFilesystem item){
        if (item instanceof NetworkFile file){
            files.remove(file.getHash());
        }else if (item instanceof NetworkFolder folder){
            folders.remove(folder.getHash());
        }
    }

    private void setupChildrenAndSort(File file) {
        addSubfolders(file);
        addFiles(file);
        sort();
    }

    //Sorts according to implementation of compareTo(). Default is alphabetical
    private void sort() {
        folders = (Map<Long, NetworkFolder>) sortByValue(folders, true);
        files = (Map<Long, NetworkFile>) sortByValue(files, true);
    }


    public NetworkFolder findFolder(long hash){
        NetworkFolder folder = folders.get(hash);
        if (folder!= null){
            return folder;
        }else{
            for (NetworkFolder subfolder: folders.values()){
                folder = subfolder.findFolder(hash);
                if (folder != null){
                    return folder;
                }
            }
        }
        return null;
    }

    public NetworkFile findFile(long hash){
        NetworkFile file = files.get(hash);
        if (file != null){
            return file;
        }else{
            for (NetworkFolder folder: folders.values()){
                file = folder.findFile(hash);
                if (file != null){
                    return file;
                }
            }
        }
        return null;
    }

    //Adds all the files of the current folder as children, if their file extension is in the extensionArray
    // If no array was provided, or if the array is empty, all files will be added
    private void addFiles(File file) {
        File [] files = file.listFiles(File::isFile);
        if (files != null) {
            for (File subfile: files){
                if (!subfile.getName().startsWith(".") && fileExtensionExistsInList(subfile.getName())) {
                    NetworkFile newFile = new NetworkFile(subfile, this.pathFromRoot, this.getRoot(), this.getHash());
                    this.files.put(newFile.getHash(), newFile);
                }
            }
        }
    }

    public void printData(NetworkFile file){
        if (file.getName().contains("h")) {
            Path path = Path.of(file.getFile().getAbsolutePath());
            try {

                BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);



                System.out.println(file.getName());
                System.out.println(attr.fileKey());
                //System.out.println(attr.fileKey().toString().split("ino=")[1].replace(")", ""));

                //view.write(name, writeBuffer);
                XAttrUtil xattr = new XAttrUtil();
                XAttrUtil.setXAttr(file.getFile().getPath(), "Hash", String.valueOf(file.getHash()));
                System.out.println(XAttrUtil.listXAttr(file.getFile().getPath()));
                System.out.println(XAttrUtil.getXAttr(file.getFile().getPath(), "Hash"));

                //Files.setAttribute(Path.of(file.getFile().getPath()), "Hash", file.getHash());
                // System.out.println(Files.readAttributes(Path.of(file.getFile().getPath()), "*"));
            } catch (IOException e) {
                e.printStackTrace();
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
                NetworkFolder newFolder = new NetworkFolder(subfolder, this.pathFromRoot, this.getRoot(), this.getHash(), extensionArray, folderNamesToIgnore);
                folders.put(newFolder.getHash(), newFolder);
            }
        }
    }

    //Returns an array containing the names of all the immediate subfolders of the current folder
    // (Non recursive items)
    private ArrayList<String> getTopLevelFolderNames(){
        ArrayList<String> folderNames = new ArrayList<>();
        for (NetworkFolder folder: folders.values()){
            folderNames.add(folder.getName());
        }
        return folderNames;
    }

    //Returns an array contianing the names of all the immediate files of the current folder
    // (Non recursive items)
    private ArrayList<String> getFileNames(){
        ArrayList<String> fileNames = new ArrayList<>();
        for (NetworkFile file: files.values()){
            fileNames.add(file.getName());
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
        JSONObject items = super.getData();
        items.put("path", getPathFromRoot());
        items.put("files", getJSONFiles());
        JSONArray subfolders = new JSONArray();

        for (NetworkFolder subfolder: folders.values()){
            subfolders.put(subfolder.getJSONItems());
        }

        items.put("subfolders", subfolders);
        return items;
    }

    // Returns a JSON array containing all the JSON data of files in current folder
    public JSONArray getJSONFiles(){
        JSONArray items = new JSONArray();
        for (Map.Entry<Long, NetworkFile> entry: files.entrySet()){
            items.put(entry.getValue().getData());
        }
        return items;
    }

    // returns all the subfolder objects (unmodifiable)
    public Collection<NetworkFolder> getFolders(){
        return Collections.unmodifiableCollection(folders.values());
    }

    //Returns all the subfile objects (unmodifiable)
    public Collection<NetworkFile> getFiles(){
        return Collections.unmodifiableCollection(files.values());
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
        return false;
    }

    //Compares file extension to array of extensions to include
    // Returns true if no array is passed, or if filename matches extension list
    // returns false otherwise
    private boolean fileExtensionExistsInList(String filename){
        if (extensionArray.length == 0){
            System.out.println("here");
            return true;
        }
        for (String extension: extensionArray){
            if (filename.endsWith(extension)){
                return true;
            }
        }
        return false;
    }

    private static Map<Long, ? extends NetworkFilesystem> sortByValue(Map<Long, ? extends NetworkFilesystem> unsortMap, final boolean order)
    {
        List<Map.Entry<Long, ? extends NetworkFilesystem>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        list.sort((o1, o2) -> order ? o1.getValue().compareTo(o2.getValue()) == 0
                ? o1.getKey().compareTo(o2.getKey())
                : o1.getValue().compareTo(o2.getValue()) : o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));

    }
}
