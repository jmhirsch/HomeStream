package controller;


import com.uwetrottmann.tmdb2.Tmdb;
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

public class TMDBData {

    private static final String moviePosterPath = "http://image.tmdb.org/t/p/w780";
    private static final String getMoviePosterPathSmall = "http://image.tmdb.org/t/p/w342";
    private static final String actorImagePath = "http://image.tmdb.org/t/p/w300";
    private static final String backdropPosterPath = "http://image.tmdb.org/t/p/w1280";


    private final Tmdb tmdb;
    private final MoviesService moviesService;
    private final SearchService searchService;
    public TMDBData() {
        tmdb = new Tmdb("w");
        moviesService = tmdb.moviesService();
        searchService = tmdb.searchService();

    }

    public MovieData getMovieDataFor(int id) throws IOException {
        MovieData data = null;

        Call<Movie> movieCall = moviesService.summary(id, null);
        Movie movie  = movieCall.execute().body();
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
        addCast(data, id);

        return data;
    }

    public MovieData getMovieDataFor(String movieTitle, NetworkFile file) throws IOException, XmlRpcException {
        MovieData data = null;

        Pattern yearPattern = Pattern.compile("(.+)\\.((19|20)[0-9]{2}).*");
        Pattern numPattern = Pattern.compile("[0-9]{1,}\\.\\s(.*)");
        Matcher m;

        Call<MovieResultsPage> result;
        if ((m = yearPattern.matcher(movieTitle)).matches()){
                String title = m.group(1).replace(".", " ").trim();
                title.replace( " - ", " ");
                String year = m.group(2);
                //System.out.println(title + ", year: " + year);
                result = searchService.movie(title, null, null, null, null, Integer.parseInt(year.trim()), null);
        }else if ((m = numPattern.matcher(movieTitle)).matches()){
            movieTitle = m.group(1).trim();
            movieTitle.replace( " - ", " ");
            //System.out.println(movieTitle);
            result = searchService.movie(movieTitle, null, null, null, null, null, null);
        } else{
            result = searchService.movie(movieTitle, null, null, null, null, null, null);
        }

        assert result!= null;
        Response<MovieResultsPage> response = result.execute();

        int id = -1;

        for (BaseMovie baseMovie : response.body().results) {

            String title = baseMovie.title;
            id = baseMovie.id;
            data = getMovieDataFor(id);
            break;
        }

        if (data == null){
            data = getDefaultData(file);
        }

        return data;
    }

    public MovieData getDefaultData(NetworkFile file){
        return new MovieData(file.getNameWithoutExtension(), "na", "unknown", "no data available", 0, 0, 0, -2, 0,
                new ArrayList<String>(),"blank poster", "blank poster" + "", "");
    }

    private void addCast(MovieData data, int id) throws IOException {
        Call<Credits> creditsCall = moviesService.credits(id);
        Credits credits = creditsCall.execute().body();
        for (CastMember castMember : credits.cast) {
            String name = castMember.name;
            String character = castMember.character;
            Integer castId = castMember.cast_id;
            Integer order = castMember.order;
            assert order != null;
            assert castId != null;
            String profileImage = castMember.profile_path;
            if (profileImage == null){
                profileImage = "blank poster";
            }else {
                profileImage = actorImagePath + profileImage;
            }
            data.addCast(new CastData(name, character, order, castId, profileImage));
            if (order >= 20){
                break;
            }
        }

        getDirectorAndProducer(data, credits);
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
