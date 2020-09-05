package model.data;

import enums.DataType;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SeasonData extends Data {
    private final String title;
    private final String overview;
    private final String airDate;
    private final int numberOfEpisodes;
    private final int seasonNumber;
    private final List<CastData> castDataList;
    private final Map<Integer, EpisodeData> episodes;
    private boolean isFavorite = false;
    private boolean hasLinkedEpisode = false;

    public SeasonData(int id, String image, String title, String overview, String airDate, int seasonNumber, int numberOfEpisodes, List<CastData> castDataList, Map<Integer, EpisodeData> episodes) {
        super(id, image);
        this.title = title;
        this.overview = overview;
        this.airDate = airDate;
        this.numberOfEpisodes = numberOfEpisodes;
        this.castDataList = castDataList;
        this.episodes = episodes;
        this.seasonNumber = seasonNumber;
    }


    public int linkEpisode(int episodeNum, long hash){
        EpisodeData episodeData = episodes.get(episodeNum);

        if (episodeData == null){
            return -1;
        }

        hasLinkedEpisode = true;
        episodeData.setAssociatedFileHash(hash);

        return 0;
    }

    public int getSeasonNumber(){
        return seasonNumber;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject data = super.toJSONObject();
        data.put("title", title);
        data.put("overview", overview);
        data.put("date", airDate);
        data.put("seasonNum", seasonNumber);
        data.put("numOfEpisodes", numberOfEpisodes);
        data.put("isFavorite", isFavorite);
        data.put("hasLinkedEpisode", hasLinkedEpisode);
        JSONArray castArray = new JSONArray();
        for (CastData castData: castDataList){
            JSONObject cast = castData.toJSONObject();
            castArray.put(cast);
        }
        data.put("cast", castArray);

        JSONArray episodeArray = new JSONArray();
        for (EpisodeData episodeData: episodes.values()){
            JSONObject episode = episodeData.toJSONObject();
            episodeArray.put(episode);
        }
        data.put("episodes", episodeArray);

        return data;
    }

    public void addCast(CastData castData){
        castDataList.add(castData);
    }

    public void addEpisode(EpisodeData episode){
        episodes.put(episode.getEpisodeNum(), episode);
    }

}
