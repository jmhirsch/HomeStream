package controller;

import interfaces.NotificationListener;
import model.NetworkFile;
import model.NetworkFilesystem;
import model.NetworkFolder;
import observer.Subject;
import org.json.JSONObject;
import services.UserDataService;

import javax.swing.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class DataController extends Subject {

    private static final String DATA_FILE_LOCATION = "data.hs";
    private final NotificationListener notificationListener;
    private NetworkFolder root;
    private NetworkFolder favorite;
    private final UserDataService userDataService;

    public DataController(NotificationListener notificationListener){
        this.notificationListener = notificationListener;
        userDataService = new UserDataService(DATA_FILE_LOCATION);
    }

    public void createData(String path, String[] extensionList, String [] foldersToIgnore) {
        root = new NetworkFolder(new File(path), extensionList, foldersToIgnore);
        favorite = new NetworkFolder(new File(path), extensionList, foldersToIgnore, true);
        load();
        System.out.println("root path: " + root.getFile().getPath());
    }

    public JSONObject getJSONData(){
        return root.getJSONItems();
    }

    public boolean updateFileAtHash(long hash, boolean isFavorite, int playbackPosition){
        NetworkFile file = getFile(hash);
        if (file == null){
            return false;
        }
        updateFavoriteStatus(isFavorite, file);
        file.setCurrentPlaybackPosition(playbackPosition);
        save();
        return true;
    }

    public JSONObject getJSONDataFavorites(){return favorite.getJSONItems();}

    public boolean updateFolderAtHash(long hash, boolean isFavorite){
        NetworkFolder folder = getFolder(hash);
        if (folder == null){
            return false;
        }
        updateFavoriteStatus(isFavorite, folder);
        save();
        return true;
    }

    private void updateFavoriteStatus(boolean isFavorite, NetworkFilesystem item) {
        item.setFavorite(isFavorite);
        if (isFavorite){
            favorite.addNetworkItem(item);
        }else{
            favorite.removeNetworkItem(item);
        }
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

    public synchronized void save(){
        SwingWorker <Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {

                userDataService.write(buildHashmap());
                return null;
            }
        };
        worker.execute();
    }

    public synchronized void load(){
        JSONObject values = userDataService.read();
        if (values != null) {
            updateData(values, root);
        }
        save();
    }

    private void updateData(JSONObject data, NetworkFolder folder){
        for (NetworkFile file: folder.getFiles()){
            JSONObject fileData = data.getJSONObject(String.valueOf(file.getHash()));
            file.setFavorite(fileData.getBoolean("isFavorite"));
            file.setCurrentPlaybackPosition(fileData.getInt("playbackPosition"));
            String name = fileData.getString("name");
            if (!name.equalsIgnoreCase(file.getName())){
                fileData.put("name", file.getName());
            }
            if (file.isFavorite()){
                favorite.addNetworkItem(file);
            }
        }

        for (NetworkFolder subfolder: folder.getFolders()){
            if (data.has(String.valueOf(subfolder.getHash()))){
                JSONObject folderData = data.getJSONObject(String.valueOf(subfolder.getHash()));
                subfolder.setFavorite(folderData.getBoolean("isFavorite"));
                String name = folderData.getString("name");
                if (!name.equalsIgnoreCase(subfolder.getName())){
                    folderData.put("name", subfolder.getName());
                }
                if (folder.isFavorite()){
                    favorite.addNetworkItem(folder);
                }
            }
            updateData(data, subfolder);
        }
    }

    private Map<Long, JSONObject> buildHashmap(){
        HashMap<Long, JSONObject> map = new HashMap<>();
        buildHashmap(map, root);
        return map;
    }

    private void buildHashmap(HashMap<Long, JSONObject> map, NetworkFolder folder){
        map.put(folder.getHash(), folder.getData());

        for (NetworkFile file: folder.getFiles()){
            map.put(file.getHash(), file.getData());
        }

        for (NetworkFolder subfolder: folder.getFolders()){
            buildHashmap(map, subfolder);
        }
    }
}
