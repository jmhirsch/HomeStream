package controller;

import enums.DataType;
import interfaces.TMDBService;
import model.data.CastData;
import model.data.MovieData;
import model.network.NetworkFile;
import org.apache.xmlrpc.XmlRpcException;
import org.json.JSONArray;
import org.json.JSONTokener;
import tmdbservices.TMDBData;
import interfaces.Loadable;
import interfaces.Saveable;
import model.network.NetworkFolder;
import org.json.JSONObject;
import services.UserDataService;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MovieDataController extends AbstractDataController implements Loadable, Saveable {

    private static final String MOVIE_DATA_FILE_LOCATION = "movie_data.hs";
    private static final String MOVIE_PREFS_FILE_LOCATION = "movie_prefs.hs";

    public MovieDataController(String apiKey){
        super( new TMDBData(apiKey), MOVIE_DATA_FILE_LOCATION, MOVIE_PREFS_FILE_LOCATION);
    }


    public void createData(String path, String[] extensionList, String[] foldersToIgnore){
        super.createData(path, extensionList, foldersToIgnore, DataType.MOVIE);
    }

    @Override
    protected void fetchDataFromNetwork(NetworkFile file, JSONObject filedata, JSONObject filePrefs) {
        getMovieData(file, filedata);
    }

    public JSONObject getJSONDataFavorites() {
        return favorite.getJSONItems();
    }

    @Override
    public synchronized void load() {
        JSONObject data = dataService.readData();
        JSONObject prefs = dataService.readPrefs();
        if (data != null && prefs != null) {
            executorService = Executors.newCachedThreadPool();
            updateData(data, prefs, root);
            executorService.shutdown();
            try {
                executorService.awaitTermination(20, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public synchronized void save() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                Map[] maps = buildHashmap();
                Map dataMap = maps[0];
                Map prefsMap = maps[1];

                dataService.writeData(new JSONObject(dataMap));
                dataService.writePrefs(new JSONObject(prefsMap));
                return null;
            }
        };
        worker.execute();
    }

    private Map[] buildHashmap() {
        HashMap<Long, JSONObject> dataMap = new HashMap<>();
        HashMap<Long, JSONObject> prefsMap = new HashMap<>();
        buildHashmap(dataMap, prefsMap, root);

        Map[] maps = {dataMap, prefsMap};

        return maps;
    }

    private void buildHashmap(Map<Long, JSONObject> dataMap, Map<Long, JSONObject> prefsMap, NetworkFolder folder) {
        prefsMap.put(folder.getHash(), folder.getData());

        for (NetworkFile file : folder.getFiles()) {
            prefsMap.put(file.getHash(), file.getFilePrefs());
            dataMap.put(file.getHash(), file.getFileData());
        }

        for (NetworkFolder subfolder : folder.getFolders()) {
            buildHashmap(dataMap, prefsMap, subfolder);
        }
    }

    private void getMovieData(NetworkFile file, JSONObject filedata) {
        if (file.getDatabasekey() == -1) {
            file.setData(tmdbService.search(file.getNameWithoutExtension()));
        } else if (file.getDatabasekey() == -2) {
            //file.setData(tmdbService.getDefaultData(file.getNameWithoutExtension()));
        } else {
            file.setData(getDataFromJSON(filedata));
        }
    }

    private MovieData getDataFromJSON(JSONObject filedata) {
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

        for (int i = 0; i < genreArray.length(); i++) {
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
}
