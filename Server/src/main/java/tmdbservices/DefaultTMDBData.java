package tmdbservices;

import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.CastMember;
import com.uwetrottmann.tmdb2.entities.Credits;
import interfaces.TMDBService;
import model.data.CastData;
import retrofit2.Call;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public abstract class DefaultTMDBData implements TMDBService {
    protected static final String moviePosterPath = "http://image.tmdb.org/t/p/w780";
    protected static final String getMoviePosterPathSmall = "http://image.tmdb.org/t/p/w342";
    protected static final String actorImagePath = "http://image.tmdb.org/t/p/w185";
    protected static final String backdropPosterPathSmall = "http://image.tmdb.org/t/p/w300";
    protected static final String backdropPosterPath = "http://image.tmdb.org/t/p/w1280";
    protected static final String episodeStillPath = "http://image.tmdb.org/t/p/w185";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private final int castNum;
    protected final Tmdb tmdb;

    public DefaultTMDBData(String apiKey, int castNum){
        this.castNum = castNum;
        tmdb = new Tmdb(apiKey);
    }


    public List<CastData> getCast(Call<Credits> creditsCall){
        ArrayList<CastData> data = new ArrayList<>();
        try {
            Credits credits = creditsCall.execute().body();
            for (CastMember castMember : credits.cast) {
                String name = castMember.name;
                String character = castMember.character;
                Integer order = castMember.order;
                String profileImage = castMember.profile_path;
                if (profileImage == null) {
                    profileImage = "blank poster";
                } else {
                    profileImage = actorImagePath + profileImage;
                }
                data.add(new CastData(name, character, order, 0, profileImage));
                if (order >= castNum) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }

    public String format(Date date){
        return dateFormat.format(date);
    }
}
