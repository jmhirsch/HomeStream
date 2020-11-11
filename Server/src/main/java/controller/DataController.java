package controller;

import enums.DataType;
import interfaces.Loadable;
import interfaces.Saveable;
import model.data.CastData;
import model.data.MovieData;
import model.data.TVShowData;
import model.data.TVShowSearchData;
import model.network.NetworkFile;
import model.network.NetworkFilesystem;
import model.network.NetworkFolder;
import netscape.javascript.JSObject;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import services.UserDataService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class DataController implements Loadable, Saveable {

    private static final String API_KEY = "173b789d94a7a1318de3aa759a1ddd79";
    private NetworkFolder root;
    private NetworkFolder favorite;


    private ExecutorService executorService;

    private final MovieDataController movieController;
    private final TVShowDataController showController;

    public DataController() {
        movieController = new MovieDataController(API_KEY);
        showController = new TVShowDataController(API_KEY);
    }

    public void createData(String moviePath, String tvShowPath, String[] extensionList, String[] foldersToIgnore) {

        movieController.createData(moviePath, extensionList, foldersToIgnore);
        showController.createData(tvShowPath, extensionList, foldersToIgnore);

        root = new NetworkFolder(new File(moviePath), extensionList, foldersToIgnore, true);
        favorite = new NetworkFolder(new File(moviePath), extensionList, foldersToIgnore, true);
        load();
    }

    public boolean updateMovieFileAtHash(long hash, boolean isFavorite, int playbackPosition){
        return movieController.updateFileAtHash(hash, isFavorite, playbackPosition);
    }

    public boolean updateMovieFolderAtHash(long hash, boolean isFavorite){
        return movieController.updateFolderAtHash(hash, isFavorite);
    }

    public void getMovieFile(){

    }

    public void getTVShowFile(){

    }

    public JSONObject getMovies(){
        return movieController.getJSONData();
    }


    public JSONObject getJSONData() {
        return root.getJSONItems();
    }

//    public boolean updateFileAtHash(long hash, boolean isFavorite, int playbackPosition) {
//        NetworkFile file = getFile(hash);
//        if (file == null) {
//            return false;
//        }
//        updateFavoriteStatus(isFavorite, file);
//        file.setCurrentPlaybackPosition(playbackPosition);
//        save();
//        return true;
//    }

    public JSONObject getJSONDataFavorites() {
        return favorite.getJSONItems();
    }

//    public boolean updateFolderAtHash(long hash, boolean isFavorite) {
//        NetworkFolder folder = getFolder(hash);
//        if (folder == null) {
//            return false;
//        }
//        updateFavoriteStatus(isFavorite, folder);
//        save();
//        return true;
//    }

//    private void updateFavoriteStatus(boolean isFavorite, NetworkFilesystem item) {
//        item.setFavorite(isFavorite);
//        if (isFavorite) {
//            favorite.addNetworkItem(item);
//        } else {
//            favorite.removeNetworkItem(item);
//        }
//    }

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

    public synchronized void save() {
        movieController.save();
    }

    public synchronized void load() {
        movieController.load();
//            for (TVShowData datas : tvshowList.values()) {
//                // System.out.println(data.toJSONObject().toString(5));
//            }
        save();
    }

//    public JSONArray getTVShowData() {
//        JSONArray data = new JSONArray();
//        for (TVShowData tvshow : tvshowList.values()) {
//            data.put(tvshow.toJSONObject());
//        }
//        return data;
//    }

    private void updateData(JSONObject data, JSONObject prefs, NetworkFolder folder) {

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
                //fetchDataFromNetwork(file, fileData, filePrefs);
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

        for (NetworkFolder subfolder : folder.getFolders()) {
            if (data.has(String.valueOf(subfolder.getHash()))) {
                JSONObject folderData = prefs.getJSONObject(String.valueOf(subfolder.getHash()));
                subfolder.setFavorite(folderData.getBoolean("isFavorite"));
                String name = folderData.getString("name");
                if (!name.equalsIgnoreCase(subfolder.getName())) {
                    folderData.put("name", subfolder.getName());
                }
                if (folder.isFavorite()) {
                    favorite.addNetworkItem(folder);
                }
            }
            updateData(data, prefs, subfolder);
        }
    }

//    private void fetchDataFromNetwork(NetworkFile file, JSONObject filedata, JSONObject filePrefs) {
//        if (file.getDataType() == DataType.MOVIE && !file.getExtension().contains(".srt")) {
//            getMovieData(file, filedata);
//        } else if (file.getDataType() == DataType.TV_SHOW && !file.getExtension().contains(".srt")) {
//            //getTVShowData(file, filePrefs);
//        }
//    }

    private void getTVShowData(NetworkFile file, JSONObject filedata) {
//        try {
//            TVShowSearchData show = tvdbData.searchForTVID(file.getNameWithoutExtension());
//            if (show.getID() != -1) {
//                synchronized (this) {
//                    if (!tvshowList.containsKey(show.getID())) {
//                        tvshowList.put(show.getID(), tvdbData.getTVDataFor(show.getID()));
//                    }
//                }
//
//                if (show.getSeason() == 6 && show.getEpisode() == 18) {
//                    System.out.println(file.getNameWithoutExtension());
//                }
//
//                if (tvshowList.get(show.getID()).linkEpisode(show.getSeason(), show.getEpisode(), file.getHash()) == -1) {
//                    System.out.println("Show: " + show.getName() + " S" + show.getSeason() + "E" + show.getEpisode());
//                }
//            }
//        } catch (Exception e) {
//            System.out.println("Error get tv show data: " + file.getNameWithoutExtension() + " ----- " + e.getLocalizedMessage() + "; " + e.getCause());
//            System.out.println(Arrays.toString(e.getStackTrace()));
//        }
    }






}
