package services;

import org.json.JSONObject;

import java.io.*;
import java.util.Map;

public class UserDataService {


    public UserDataService(){

    }

    public void write(Map<Long, JSONObject> map){
        try {
            FileWriter writer = new FileWriter(new File("preferences"));

            JSONObject objectToPrint  = new JSONObject();
            for (long key: map.keySet()){
                objectToPrint.put(String.valueOf(key), map.get(key).toString() + '\n');
            }

            writer.write(objectToPrint.toString(4));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public JSONObject read(){
        JSONObject object = new JSONObject();
        try {
            FileReader reader = new FileReader("preferences");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return object;
    }





}
