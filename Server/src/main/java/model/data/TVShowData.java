package model.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class TVShowData extends DefaultData {
    private final String title;
    private final String overview;
    private final String language;
    private final String status;
    private final String firstAired;
    private final String lastAired;
    private final String nextToAir;
    private final int runtime;
    private final int numberOfEpisodes;
    private final int numberOfSeasons;
    private final String backdrop;
    private final boolean inProduction;
    private final String smallBackdrop;
    private boolean isFavorite = false;
    private boolean hasLinkedEpisode = false;

    private Map<Integer, SeasonData> seasons;

    public TVShowData(int id, String image, String title, String overview, String language, String status, String firstAired, String lastAired, String nextToAir, int runtime, int numberOfEpisodes, int numberOfSeasons, boolean inProduction, String backdrop, String smallBackdrop) {
        super(id, image);
        this.title = title;
        this.overview = overview;
        this.language = language;
        this.status = status;
        this.firstAired = firstAired;
        this.lastAired = lastAired;
        this.nextToAir = nextToAir;
        this.runtime = runtime;
        this.numberOfEpisodes = numberOfEpisodes;
        this.numberOfSeasons = numberOfSeasons;
        this.backdrop = backdrop;
        this.inProduction = inProduction;
        this.smallBackdrop = smallBackdrop;
        seasons =  new HashMap<>();
    }

    public void addSeason(SeasonData data){
        seasons.put(data.getSeasonNumber(), data);
    }


    public int linkEpisode(int seasonNum, int episodeNum, long hash){
        SeasonData seasonData = seasons.get(seasonNum);

        if (seasonData == null){
            return -1;
        }
        int returnVal = seasonData.linkEpisode(episodeNum, hash);

        if (returnVal != -1){
            hasLinkedEpisode = true;
        }

        return seasonData.linkEpisode(episodeNum, hash);
    }

    public void setSeasons(Map<Integer, SeasonData> seasons){
        this.seasons = seasons;
    }

    public JSONObject toJSONObject(){
        JSONObject data = super.toJSONObject();
        data.put("title", title);
        data.put("overview", overview);
        data.put("language", language);
        data.put("status", status);
        data.put("firstAired", firstAired);
        data.put("nextToAir", nextToAir);
        data.put("lastAired", lastAired);
        data.put("runtime", runtime);
        data.put("numberOfEpisodes", numberOfEpisodes);
        data.put("numberOfSeasons", numberOfSeasons);
        data.put("backdrop", backdrop);
        data.put("smallBackdrop", backdrop);
        data.put("inProduction", inProduction);
        data.put("isFavorite", isFavorite);
        data.put("hasLinkedEpisode", hasLinkedEpisode);

        JSONArray seasonData = new JSONArray();
        for (SeasonData season: seasons.values()){
            seasonData.put(season.toJSONObject());
        }
        data.put("seasons", seasonData);

        return data;
    }
}
