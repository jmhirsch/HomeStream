package model.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class EpisodeData extends DefaultData {
    private final String overview;
    private final String airDate;
    private final int episodeNum;
    private final int seasonNum;
    private final int showID;
    private final String title;

    private final List<CastData> castDataList;
    private long associatedFileHash = -1;

    public EpisodeData(int id, String image, String title, String overview, String airDate, int episodeNum, int seasonNum, int showID, List<CastData> castDataList) {
        super(id, image);
        this.overview = overview;
        this.airDate = airDate;
        this.episodeNum = episodeNum;
        this.seasonNum = seasonNum;
        this.showID = showID;
        this.castDataList = castDataList;
        this.title = title;
    }

    public int getEpisodeNum() {
        return episodeNum;
    }

    @Override
    public JSONObject toJSONObject() {
        JSONObject data = super.toJSONObject();
        data.put("title", title);
        data.put("overview", overview);
        data.put("date", airDate);
        data.put("episodeNum", episodeNum);
        data.put("seasonNum", seasonNum);
        data.put("showID", showID);
        data.put("filehash", associatedFileHash);
        JSONArray castArray = new JSONArray();
        for (CastData castData: castDataList){
            JSONObject cast = castData.toJSONObject();
            castArray.put(cast);
        }
        data.put("cast", castArray);
        return data;
    }


    public void addCast(CastData castData){
        castDataList.add(castData);
    }

    public boolean hasEpisode(){
        return associatedFileHash != -1;
    }

    public long getAssociatedFileHash() {
        return associatedFileHash;
    }

    public void setAssociatedFileHash(long associatedFileHash) {
        this.associatedFileHash = associatedFileHash;
    }
}
