package model.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MovieData extends DefaultData {

    private final String title;
    private final String originalLanguage;
    private final String date;
    private final String description;
    private List<CastData> castDataList;
    private final long budget;
    private final long boxOffice;
    private final int runtime;
    private final String smallImage;
    private final String backdrop;
    private final double rating;
    private  String producers = "Unknwon";
    private  String director = "Unknown";
    private final List<String> genre;


    public MovieData(String title, String originalLanguage, String date, String description, long budget, long boxOffice, int runtime, int id, double rating, List<String> genre, String image, String smallImage, String backdrop) {
        super(id, image);
        this.title = title;
        this.originalLanguage = originalLanguage;
        this.date = date;
        this.description = description;
        this.budget = budget;
        this.boxOffice = boxOffice;
        this.runtime = runtime;
        this.smallImage = smallImage;
        this.backdrop = backdrop;
        this.rating = rating;
        this.genre = genre;
        castDataList = new ArrayList<>(20);
    }

    public MovieData(String title, String originalLanguage, String date, String description, long budget, long boxOffice, int runtime, int id, double rating, String director, String producers, List<String> genre, List<CastData> castData, String image, String smallImage, String backdrop){
        super(id, image);
        this.title = title;
        this.originalLanguage = originalLanguage;
        this.date = date;
        this.description = description;
        this.budget = budget;
        this.boxOffice = boxOffice;
        this.runtime = runtime;
        this.castDataList = castData;
        this.smallImage = smallImage;
        this.rating = rating;
        this.genre = genre;
        this.backdrop = backdrop;
        this.director = director;
        this.producers = producers;
    }

    public void addDirector(String director){
        this.director = director;
    }

    public void addProducers(String producers){
        this.producers = producers;
    }

    @Override
    public JSONObject toJSONObject() {
       JSONObject object = super.toJSONObject();
       object.put("title", title);
       object.put("language", originalLanguage);
       object.put("description", description);
       object.put("date", date);
       object.put("runtime", runtime);
       object.put("boxoffice", boxOffice);
       object.put("budget", budget);
       object.put("rating", rating);
       object.put("smallPoster", smallImage);
       object.put("backdrop", backdrop);
       object.put("genres", new JSONArray(genre));
       object.put("director", director);
       object.put("producers", producers);
       JSONArray castArray = new JSONArray();

       for (CastData data: castDataList){
           JSONObject cast = data.toJSONObject();
           castArray.put(cast);
       }
       object.put("cast", castArray);
       return object;
    }

    public void setCast(List<CastData> cast){
        this.castDataList = cast;
    }

    public void addCast(CastData castData){
        castDataList.add(castData);
    }
}
