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

    private static final String MOVIE_DATA_FILE_LOCATION = "movie_data.hs";
    private static final String MOVIE_PREFS_FILE_LOCATION = "movie_prefs.hs";
    private NetworkFolder root;
    private NetworkFolder favorite;
    private final UserDataService movieDataService;
    private final TMDBData tmdbData;
    private final TVDBData tvdbData;
    private final HashMap<Integer, TVShowData> tvshowList = new HashMap();
    private ExecutorService executorService;

    public DataController(){
        movieDataService = new UserDataService(MOVIE_DATA_FILE_LOCATION, MOVIE_PREFS_FILE_LOCATION);
        tmdbData = new TMDBData();
        tvdbData = new TVDBData();
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
        SwingWorker <Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                movieDataService.write(buildHashmap());
                return null;
            }
        };
        worker.execute();
    }

    public synchronized void load(){
        JSONObject values = movieDataService.read();
        long startTime = System.currentTimeMillis();
        if (values != null) {
            executorService = Executors.newCachedThreadPool();
            updateData(values, root);
            executorService.shutdown();
            try {
                executorService.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            long endTime = System.currentTimeMillis();

            System.out.println("Elapsed: " + (endTime - startTime));

            for (TVShowData data: tvshowList.values()){
               // System.out.println(data.toJSONObject().toString(5));
            }
        }
        save();
    }

    public JSONArray getTVShowData(){
        JSONArray data = new JSONArray();
        for (TVShowData tvshow: tvshowList.values()){
            data.put(tvshow.toJSONObject());
        }
        return data;
    }

    private void updateData(JSONObject data, NetworkFolder folder){

        for (NetworkFile file: folder.getFiles()){
                try {
                    JSONObject fileData = data.getJSONObject(String.valueOf(file.getHash()));
                    file.setFavorite(fileData.getBoolean("isFavorite"));
                    file.setCurrentPlaybackPosition(fileData.getInt("playbackPosition"));
                    file.setDatabasekey(fileData.getInt("databaseKey"));
                    fetchDataFromNetwork(file, fileData);
                    String name = fileData.getString("name");
                    if (!name.equalsIgnoreCase(file.getName())) {
                        fileData.put("name", file.getName());
                    }
                    if (file.isFavorite()) {
                        favorite.addNetworkItem(file);
                    }
                }catch(Exception e){
                    System.out.println("Error  updating file data: " + file.getData().toString(4) + " \n, ");
                    e.printStackTrace();
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

    private void fetchDataFromNetwork(NetworkFile file, JSONObject filedata){
        if (file.getDataType() == DataType.MOVIE && !file.getExtension().contains(".srt")) {
            getMovieData(file, filedata);
        }else if (file.getDataType() == DataType.TV_SHOW && !file.getExtension().contains(".srt")){
            getTVShowData(file, filedata);
        }
    }

    private void getTVShowData(NetworkFile file, JSONObject filedata){
        try{
            TVShowSearchData show = tvdbData.searchForTVID(file.getNameWithoutExtension());
            if (show.id() != -1){
                synchronized (this) {
                    if (!tvshowList.containsKey(show.id())) {
                        tvshowList.put(show.id(), tvdbData.getTVDataFor(show.id()));
                    }
                }

                if (show.season() == 6 && show.episode() == 18){
                    System.out.println(file.getNameWithoutExtension());
                }

                 if (tvshowList.get(show.id()).linkEpisode(show.season(), show.episode(), file.getHash()) == -1){
                     System.out.println("Show: " + show.name() + " S" + show.season() + "E" +show.episode());
                }
            }
        }catch (Exception e){
            System.out.println("Error get tv show data: " + file.getNameWithoutExtension() + " ----- " + e.getLocalizedMessage() + "; " + e.getCause());
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private void getMovieData(NetworkFile file, JSONObject filedata) {
        try {
            if (file.getDatabasekey() == -1){
                file.setData(tmdbData.getMovieDataFor(file.getNameWithoutExtension(), file));
            }else if (file.getDatabasekey() == -2) {
                file.setData(tmdbData.getDefaultData(file));
            }else{
                file.setData(getDataFromJSON(filedata));
            }
        } catch (IOException | XmlRpcException e) {
            System.out.println("Error: " + e.getLocalizedMessage());
        }
    }

    private MovieData getDataFromJSON(JSONObject filedata){
         filedata = filedata.getJSONObject("data");
         String title = filedata.getString("title");
         String language = filedata.getString("language");
         String description = filedata.getString("description");
         String date = filedata.getString("date");
         int runtime = filedata.getInt("runtime");
         int id = filedata.getInt("id");
         long budget = filedata.getLong("budget");
         long boxOffice = filedata.getLong("boxoffice");
         String poster = filedata.getString("image");
         String smallPoster = filedata.getString("smallPoster");
         String backdrop = filedata.getString("backdrop");
         double rating = filedata.getDouble("rating");
         String director = filedata.getString("director");
         String producers = filedata.getString("producers");

         List<String> genre = new ArrayList<>();
         JSONArray genreArray = new JSONArray(new JSONTokener(filedata.get("genres").toString()));

         for (int i = 0; i < genreArray.length(); i++){
             genre.add(genreArray.getString(i));
         }

        List<CastData> castDataList = new ArrayList<>();
        JSONArray array = new JSONArray(new JSONTokener(filedata.get("cast").toString()));

        for (Iterator<Object> it = array.iterator(); it.hasNext(); ) {
            JSONObject cast = new JSONObject(new JSONTokener(it.next().toString()));
            String name = cast.getString("name");
            String character = cast.getString("character");
            int order = cast.getInt("order");
            int castid = cast.getInt("id");
            String image = cast.getString("image");
            castDataList.add(new CastData(name, character, order, castid, image));
        }
        return new MovieData(title, language, date, description, budget, boxOffice, runtime, id, rating, director, producers, genre, castDataList, poster, smallPoster, backdrop);
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
