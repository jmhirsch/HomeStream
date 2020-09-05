package controller;

import com.github.wtekiela.opensub4j.api.OpenSubtitlesClient;
import com.uwetrottmann.tmdb2.Tmdb;
import com.uwetrottmann.tmdb2.entities.*;
import com.uwetrottmann.tmdb2.services.*;
import model.data.*;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TVDBData {

    private static final String moviePosterPath = "http://image.tmdb.org/t/p/w500";
    private static final String actorImagePath = "http://image.tmdb.org/t/p/w185";
    private static final String backdropPosterPathSmall = "http://image.tmdb.org/t/p/w300";
    private static final String backdropPosterPath = "http://image.tmdb.org/t/p/w1280";
    private static final String episodeStillPath = "http://image.tmdb.org/t/p/w185";

    private final Tmdb tmdb;
    private final TvService tvService;
    private final TvSeasonsService seasonsService;
    private final TvEpisodesService episodesService;
    private final SearchService searchService;
    OpenSubtitlesClient osClient;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private final ExecutorService executorService;

    public TVDBData() {
        tmdb = new Tmdb("173b789d94a7a1318de3aa759a1ddd79");
        tvService = tmdb.tvService();
        searchService = tmdb.searchService();
        episodesService = tmdb.tvEpisodesService();
        seasonsService = tmdb.tvSeasonsService();

        executorService = Executors.newCachedThreadPool();
    }


    public TVShowSearchData searchForTVID(String showTitle) {

        //showTitle = "castle.2009.S03e06.1080p.hdtv.h264-mtb";

        Pattern yearPattern = Pattern.compile("(.*)\\.(19|20\\d{2}).*[s|S](\\d+)[e|E](\\d+).*");
        Pattern noYearPattern = Pattern.compile("(.*)\\.[s|S](\\d+)[e|E](\\d+).*");
        Pattern formattedPattern = Pattern.compile("(.*)-.*[s|S](\\d+)[e|E](\\d+).*");
        Matcher matcher;

        Call<TvShowResultsPage> tvSearchCall = null;
        String name;
        int season;
        int episode;

        if ((matcher = yearPattern.matcher(showTitle)).matches()){
             name = matcher.group(1).replaceAll("\\.", " ").trim();
            int year = Integer.parseInt(matcher.group(2).replaceAll("\\.", " ").trim());
             season = Integer.parseInt(matcher.group(3));
             episode = Integer.parseInt(matcher.group(4));
            tvSearchCall = searchService.tv(name, null, null, year);

        }else if((matcher = noYearPattern.matcher(showTitle)).matches()){

             name = matcher.group(1).replaceAll("\\.", " ").trim();
             season = Integer.parseInt(matcher.group(2));
             episode = Integer.parseInt(matcher.group(3));

            tvSearchCall = searchService.tv(name, null, null, null);

        }else if(((matcher = formattedPattern.matcher(showTitle)).matches())){
             name = matcher.group(1).trim();
             season = Integer.parseInt(matcher.group(2).trim());
             episode = Integer.parseInt(matcher.group(3).trim());
            tvSearchCall = searchService.tv(name, null, null, null);
        }else{
            return new TVShowSearchData("na", -1, -1, -1);
        }


        //System.out.println("name: " + name + " S" + season + "E" + episode);
        try {
            Response<TvShowResultsPage> tvShowResponse = tvSearchCall.execute();

            assert tvShowResponse.body() != null;
            assert tvShowResponse.body().results != null;
            for (BaseTvShow show : tvShowResponse.body().results) {
                return new TVShowSearchData(show.name, show.id, season, episode);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public TVShowData getTVDataFor(int id) {
        TVShowData data = null;
        Call<TvShow> tvCall = tvService.tv(id, null);
        try {
            TvShow show = tvCall.execute().body();
            String name = show.name;
            String image = moviePosterPath + show.poster_path;
            String overview = show.overview;
            String status = show.status;
            Date firstAired = show.first_air_date;
            Boolean inProduction = show.in_production;
            Date lastAired = show.last_air_date;
            String language = show.original_language;
            int runtime = show.episode_run_time.get(0);
            String nextToAirDate = "";
            String backdrop = backdropPosterPath;
            String smallBackdrop = backdropPosterPathSmall;
            String backdropText = getTextBackdrop(id, language);
            if (backdropText.equals("none")){
                backdrop += show.backdrop_path;
                smallBackdrop += show.backdrop_path;
            }else{
                backdrop += backdropText;
                smallBackdrop += backdropText;
            }

            int numberOfSeasons = show.number_of_seasons;
            int numberOfEpisodes = show.number_of_episodes;

            String firstAiredDate = dateFormat.format(firstAired);
            String lastAiredDate = dateFormat.format(lastAired);


            if (inProduction && show.next_episode_to_air != null) {
                nextToAirDate = dateFormat.format(show.next_episode_to_air.air_date);
            } else {
                nextToAirDate = "na";
            }

            data = new TVShowData(id, image, name, overview, language, status, firstAiredDate, lastAiredDate, nextToAirDate, runtime, numberOfEpisodes, numberOfSeasons, inProduction, backdrop, smallBackdrop);
            data.setSeasons(getSeasonsData(show.seasons, id));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public String getTextBackdrop(int id, String language)  {
        String backdrop = "none";
        try {
            Call<Images> imagesCall = tvService.images(id, language);
            String tempImage = imagesCall.execute().body().backdrops.get(0).file_path;

            if (tempImage != null) {
                backdrop = tempImage;
            }
        }catch(Exception e){

        }
        return backdrop;
    }

    public Map<Integer, SeasonData> getSeasonsData(java.util.List<TvSeason> seasonList, int showID){
        Map<Integer, SeasonData> data = new HashMap<>(seasonList.size());

            for (TvSeason season : seasonList) {
                try {
                    String name = season.name;
                    int seasonNumber = season.season_number;
                    if (seasonNumber <= 0){
                        continue;
                    }
                    String overview = season.overview;
                    String image = moviePosterPath + season.poster_path;
                    int id = season.id;
                    String airDate = dateFormat.format(season.air_date);
                    int numberOfEpisodes = season.episode_count;
                    java.util.List<CastData> cast = getCastSeason(showID, seasonNumber);
                    Map<Integer, EpisodeData> episodes = getEpisodesData(showID, seasonNumber, numberOfEpisodes);
                    data.put(seasonNumber, new SeasonData(id, image, name, overview, airDate, seasonNumber, numberOfEpisodes, cast, episodes));
                }catch (Exception e) {
                    System.out.println(e.fillInStackTrace() + "\n " + e.getSuppressed() + "\n " + e.getLocalizedMessage() + " in getSeason for show " + showID );
                }
             }

        return data;
    }

    public Map<Integer, EpisodeData> getEpisodesData(int showID, int seasonNum, int episodeCount){
        Map<Integer, EpisodeData> data = new HashMap<>(episodeCount);
        for (int i = 1; i <= episodeCount; i++){
            final int currentEpisodeNum = i;
            executorService.submit(() -> {
                try {
                    Call<TvEpisode> episodeCall = episodesService.episode(showID, seasonNum, currentEpisodeNum, null);
                    TvEpisode episode = episodeCall.execute().body();
                    String name = episode.name;
                    String overview = episode.overview;
                    int episodeNum = episode.episode_number;
                    int season = episode.season_number;
                    String airDate = dateFormat.format(episode.air_date);
                    String image = episodeStillPath + episode.still_path;
                    java.util.List<CastData> cast = getCastEpisode(showID, seasonNum, episodeNum);
                    data.put(episodeNum, new EpisodeData(0, image, name, overview, airDate, episodeNum, season, showID, cast));
                }catch(Exception e){
                    System.out.println(e.fillInStackTrace() + " in getEpisode for show " +showID  + "season " + seasonNum);
                }
            });

        }
        return data;
    }

    public ArrayList<CastData> getCastEpisode(int id, int seasonNum, int episodeNum){
        Call<Credits> creditsCall = episodesService.credits(id, seasonNum, episodeNum);
        return getCast(creditsCall);
    }

    public ArrayList<CastData> getCastSeason(int id, int seasonNum) {
        Call<Credits> creditsCall = seasonsService.credits(id, seasonNum);
        return getCast(creditsCall);
    }

    private ArrayList<CastData> getCast(Call<Credits> creditsCall){
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
                if (order >= 20) {
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }
}
