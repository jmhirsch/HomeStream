package tmdbservices;


import com.uwetrottmann.tmdb2.entities.*;
import com.uwetrottmann.tmdb2.services.MoviesService;
import com.uwetrottmann.tmdb2.services.SearchService;
import model.data.CastData;
import model.data.MovieData;
import model.network.NetworkFile;
import org.apache.xmlrpc.XmlRpcException;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TMDBData extends DefaultTMDBData {

    private static final int castNum = 20;


    private final MoviesService moviesService;
    private final SearchService searchService;

    public TMDBData(String apiKey) {
        super(apiKey, castNum);
        moviesService = tmdb.moviesService();
        searchService = tmdb.searchService();

    }

    public MovieData findWithID(int id) {
        MovieData data = null;

        Call<Movie> movieCall = moviesService.summary(id, null);
        Movie movie;
        try {
            movie = movieCall.execute().body();

        String title = movie.title;
        Date date = movie.release_date;
        String overview = movie.overview;
        String language = movie.original_language;
        String posterPath = movie.poster_path;
        String backdrop = movie.backdrop_path;
        long budget = movie.budget;
        long boxOffice = movie.revenue;
        int runtime = movie.runtime;
        double rating = movie.vote_average;
        List<Genre> genreList = movie.genres;
        List<String> genres = new ArrayList<>(genreList.size());
        for (Genre genre: genreList){
            genres.add(genre.name);
        }


        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        String newDate = format.format(date);
        data = new MovieData(title, language, newDate, overview, budget, boxOffice, runtime, id, rating, genres, moviePosterPath + posterPath, getMoviePosterPathSmall + posterPath, backdropPosterPath + backdrop);
        List<CastData> cast = getCast(data, id);
        data.setCast(cast);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public MovieData search(String movieTitle)  {
        MovieData data = null;
        String originalTitle = movieTitle;

        Pattern yearPattern = Pattern.compile("(.+)\\.((19|20)[0-9]{2}).*");
        Pattern numPattern = Pattern.compile("[0-9]{1,}\\.\\s(.*)");
        Matcher m;

        Call<MovieResultsPage> result;
        if ((m = yearPattern.matcher(movieTitle)).matches()){
                String title = m.group(1).replace(".", " ").trim();
                title.replace( " - ", " ");
                String year = m.group(2);
                result = searchService.movie(title, null, null, null, null, Integer.parseInt(year.trim()), null);
        }else if ((m = numPattern.matcher(movieTitle)).matches()){
            movieTitle = m.group(1).trim();
            movieTitle.replace( " - ", " ");
            result = searchService.movie(movieTitle, null, null, null, null, null, null);
        } else{
            result = searchService.movie(movieTitle, null, null, null, null, null, null);
        }

        assert result!= null;
        Response<MovieResultsPage> response = null;
        int id = -1;
        try {
            response = result.execute();
            for (BaseMovie baseMovie : response.body().results) {
                id = baseMovie.id;
                data = findWithID(id);
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (data == null){
            data = getDefaultData(originalTitle);
        }

        return data;
    }

    public MovieData getDefaultData(String title){
        return new MovieData(title, "na", "unknown", "no data available", 0, 0, 0, -2, 0,
                new ArrayList<String>(),"blank poster", "blank poster" + "", "");
    }

    private List<CastData> getCast(MovieData data, int id) throws IOException {
        Call<Credits> creditsCall = moviesService.credits(id);
        getDirectorAndProducer(data, creditsCall.execute().body());
        return getCast(creditsCall);
    }

    private void getDirectorAndProducer(MovieData data, Credits credits){
        String director = "Unknown";
        ArrayList<String> producers = new ArrayList<>();

        for (CrewMember member: credits.crew){
            if (member.job.equals("Director")){
               director = member.name;
            }else if (member.job.equals("Executive Producer") && producers.size() < 3){
                producers.add(member.name);
            }

            if (!director.equals("") && producers.size() >= 3){
                break;
            }
        }

        if (producers.size() == 0){
            producers.add("Unknown");
        }

        StringBuilder builder = new StringBuilder();

        for (String producer: producers){
            if (!builder.toString().equals("")){
                builder.append(", ");
            }
            builder.append(producer);
        }

        data.addDirector(director);
        data.addProducers(builder.toString());
    }
}
