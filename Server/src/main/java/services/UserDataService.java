package services;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

public class UserDataService {


    private final String data_file_location;
    public UserDataService(String data_file_location){
        this.data_file_location = data_file_location;
    }

    public void write(Map<Long, JSONObject> map){
        write(new JSONObject(map));
    }

    public void write(JSONObject object){
        try {
            FileWriter writer = new FileWriter(new File(data_file_location));
            object.write(writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    public JSONObject read(){
        JSONObject data = null;
        try {
            if (Files.exists(Path.of(data_file_location))) {
                System.out.println("File exists!");
                InputStream is = new BufferedInputStream(new FileInputStream(data_file_location));
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
