package controller;

import enums.FileType;
import interfaces.NotificationListener;
import model.NetworkFile;
import model.NetworkFolder;
import observer.Subject;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DataController extends Subject {


    private final NotificationListener notificationListener;
    private NetworkFolder root;

    public DataController(NotificationListener notificationListener){
        this.notificationListener = notificationListener;
    }

    public void createData(String path, String[] extensionList, String [] foldersToIgnore) {
        root = new NetworkFolder(new File(path), extensionList, foldersToIgnore);
        System.out.println("root path: " + root.getFile().getPath());
    }

    public Map<FileType, List<String>> getFilesAndFolderHashes(){
        Map<FileType, List<String>> map = new HashMap<>();
        ArrayList<String> fileHashList = new ArrayList<>();
        ArrayList<String> folderHashList = new ArrayList<>();
        map.put(FileType.FILE, fileHashList);
        map.put(FileType.FOLDER, folderHashList);
        getFolderHashes(map, root);
        return map;
    }

    public JSONObject getJSONData(){
        return root.getJSONItems();
    }

    private void getFolderHashes(Map<FileType, List<String>> map, NetworkFolder folder){
        getFileHashes(map.get(FileType.FILE), folder);

        for (NetworkFolder subfolder: folder.getFolders()){
            map.get(FileType.FOLDER).add(String.valueOf(subfolder.getHash()));
            getFolderHashes(map, subfolder);
        }
    }

    private void getFileHashes(List<String> fileHashlist, NetworkFolder folder){
        for (NetworkFile file: folder.getFiles()){
            fileHashlist.add(String.valueOf(file.getHash()));
        }
    }

    public boolean updateFileAtHash(long hash, boolean isFavorite, int playbackPosition){
        NetworkFile file = root.findFile(hash);
        if (file == null){
            return false;
        }
        file.setFavorite(isFavorite);
        file.setCurrentPlaybackPosition(playbackPosition);
        return true;
    }

    public boolean updateFolderAtHash(long hash, boolean isFavorite){
        NetworkFolder folder = root.findFolder(hash);
        if (folder == null){
            return false;
        }
        folder.setFavorite(isFavorite);
        return true;
    }

    public NetworkFile getFile(long hash){
        NetworkFile file;
        if ((file = root.findFile(hash))!= null){
            return file;
        }
        return null;
    }

    public NetworkFolder getFolder(long hash){
        NetworkFolder folder;
        if ((folder = root.findFolder(hash))!= null){
            return folder;
        }
        return null;
    }

//    private void buildHashmap(){
//        HashMap<Long, JSONObject> map = new HashMap<>();
//        buildHashmap(map, root);
//
//        UserDataService service = new UserDataService();
//        service.write(map);
//    }
//
//    private void buildHashmap(HashMap<Long, JSONObject> map, NetworkFolder folder){
//        map.put(folder.getHash(), folder.getData());
//
//        for (NetworkFile file: folder.getFiles()){
//            map.put(file.getHash(), file.getData());
//        }
//
//        for (NetworkFolder subfolder: folder.getFolders()){
//            buildHashmap(map, subfolder);
//        }
//    }
}
