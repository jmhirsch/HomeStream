package services;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDataService {


    private final String movieData_file_location;
    private final String moviePreferences_file_location;
    public UserDataService(String movieData_file_location, String moviePreferences_file_location){
        this.movieData_file_location = movieData_file_location;
        this.moviePreferences_file_location = moviePreferences_file_location;
    }


    public void writeData(JSONObject object){
        write(object, movieData_file_location);
    }

    public void writePrefs(JSONObject object){
        write(object, moviePreferences_file_location);
    }

    private void write(JSONObject object, String location){
        try {
            FileWriter writer = new FileWriter(new File(location));
            object.write(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public JSONObject readData(){
        return read(movieData_file_location);
    }

    public JSONObject readPrefs(){
        return read(moviePreferences_file_location);
    }

    private JSONObject read(String location){
        JSONObject data = null;
        try {
            if (Files.exists(Path.of(location))) {
                System.out.println("File exists!");
                InputStream is = new BufferedInputStream(new FileInputStream(location));
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                String requestStr = reader.lines().collect(Collectors.joining("\n"));
                JSONTokener parser = new JSONTokener(requestStr);
                data = new JSONObject(parser);
                is.close();
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }





}
