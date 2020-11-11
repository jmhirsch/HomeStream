package controller;

import enums.DataType;
import interfaces.Loadable;
import interfaces.Saveable;
import interfaces.TMDBService;
import model.network.NetworkFile;
import model.network.NetworkFilesystem;
import model.network.NetworkFolder;
import org.json.JSONObject;
import services.UserDataService;

import java.io.File;
import java.util.concurrent.ExecutorService;

public abstract class AbstractDataController implements Loadable, Saveable {

protected final TMDBService tmdbService;
protected final UserDataService dataService;
protected NetworkFolder root;
protected NetworkFolder favorite;
protected ExecutorService executorService;

    public AbstractDataController(TMDBService service, String dataPath, String prefPath){
        this.tmdbService = service;
        dataService = new UserDataService(dataPath, prefPath);
    }

    public void createData(String path, String [] extensionList, String [] foldersToIgnore, DataType subfilesDataType){
        root = new NetworkFolder(new File(path), extensionList, foldersToIgnore, subfilesDataType);
        favorite = new NetworkFolder(new File(path), extensionList, foldersToIgnore, true);
    }

    public JSONObject getJSONData() {
        return root.getJSONItems();
    }

    public boolean updateFileAtHash(long hash, boolean isFavorite, int playbackPosition) {
        NetworkFile file = getFile(hash);
        if (file == null) {
            return false;
        }
        updateFavoriteStatus(isFavorite, file);
        file.setCurrentPlaybackPosition(playbackPosition);
        save();
        return true;
    }

    private void updateFavoriteStatus(boolean isFavorite, NetworkFilesystem item) {
        item.setFavorite(isFavorite);
        if (isFavorite) {
            favorite.addNetworkItem(item);
        } else {
            favorite.removeNetworkItem(item);
        }
    }

    public boolean updateFolderAtHash(long hash, boolean isFavorite) {
        NetworkFolder folder = getFolder(hash);
        if (folder == null) {
            return false;
        }
        updateFavoriteStatus(isFavorite, folder);
        save();
        return true;
    }

    public NetworkFile getFile(long hash) {
        NetworkFile file;
        if ((file = root.findFile(hash)) != null) {
            return file;
        }
        return null;
    }

    public NetworkFolder getFolder(long hash) {
        NetworkFolder folder;
        if ((folder = root.findFolder(hash)) != null) {
            return folder;
        }
        return null;
    }

    protected void updateData(JSONObject data, JSONObject prefs, NetworkFolder folder) {

        for (NetworkFile file : folder.getFiles()) {
            try {
                JSONObject fileData = new JSONObject();

                if (data.has(String.valueOf(file.getHash()))) {
                    fileData = data.getJSONObject(String.valueOf(file.getHash()));
                }
                JSONObject filePrefs = prefs.getJSONObject(String.valueOf(file.getHash()));
                file.setFavorite(filePrefs.getBoolean("isFavorite"));
                file.setCurrentPlaybackPosition(filePrefs.getInt("playbackPosition"));
                file.setDatabasekey(filePrefs.getInt("databaseKey"));
                fetchDataFromNetwork(file, fileData, filePrefs);
                String name = filePrefs.getString("name");
                if (!name.equalsIgnoreCase(file.getName())) {
                    filePrefs.put("name", file.getName());
                }
                if (file.isFavorite()) {
                    favorite.addNetworkItem(file);
                }
            } catch (Exception e) {
                System.out.println("Error  updating file data: " + file.getData().toString(4) + " \n, ");
                e.printStackTrace();
            }
        }
    }

    protected abstract void fetchDataFromNetwork(NetworkFile file, JSONObject filedata, JSONObject filePrefs);
}
