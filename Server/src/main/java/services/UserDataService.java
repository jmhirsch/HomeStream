package services;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class UserDataService {


    private final String dataPath;
    private final String prefPath;
    public UserDataService(String dataPath, String prefPath){
        this.dataPath = dataPath;
        this.prefPath = prefPath;
    }


    public void writeData(JSONObject object){
        write(object, dataPath);
    }

    public void writePrefs(JSONObject object){
        write(object, prefPath);
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
        return read(dataPath);
    }

    public JSONObject readPrefs(){
        return read(prefPath);
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
