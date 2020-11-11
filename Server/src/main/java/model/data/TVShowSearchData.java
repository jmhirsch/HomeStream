package model.data;

import interfaces.Data;
import interfaces.TMDBService;
import org.json.JSONObject;


public class TVShowSearchData implements Data{

    private final String name;
    private final int ID;
    private final int season;
    private final int episode;

    public TVShowSearchData(String name, int ID, int season, int episode){
        this.name = name;
        this.ID = ID;
        this.season = season;
        this.episode = episode;
    }

    public String getName() {
        return name;
    }

    public int getID() {
        return ID;
    }

    public int getSeason() {
        return season;
    }

    public int getEpisode() {
        return episode;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public JSONObject toJSONObject() {
        return null;
    }
}