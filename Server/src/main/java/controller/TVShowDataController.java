package controller;

import tmdbservices.TVDBData;
import interfaces.Loadable;
import interfaces.Saveable;
import model.data.TVShowData;
import model.network.NetworkFolder;

import java.io.File;
import java.util.HashMap;

public class TVShowDataController implements Loadable, Saveable {
    private final TVDBData tvdbData;
    private final HashMap<Integer, TVShowData> tvshowList = new HashMap();
    private NetworkFolder root;

    public TVShowDataController(String apiKey){
        tvdbData = new TVDBData(apiKey);
    }


    public void createData(String path, String[] extensionList, String[] foldersToIgnore){
        //root = new NetworkFolder(new File(path), extensionList, foldersToIgnore);
    }

    @Override
    public synchronized void load() {

    }

    @Override
    public synchronized void save() {

    }
}
