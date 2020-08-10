package services;

import controller.Controller;
import enums.Property;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

//TODO: Property service should not be a singleton, but should be created and passed as a parameter if necessary
public class PropertyService {
    public static PropertyService instance;

    private static final String PROPERTY_FILE = "config.properties";

    private Properties properties;
    private Properties defaultProperties;

    private PropertyService(){
       properties = new Properties();
       defaultProperties = new Properties();
       createDefaults();

       if (!configExists()){
           restoreDefaults();
       }
    }

    public static PropertyService getInstance(){
        if (instance == null){
            instance = new PropertyService();
        }
        return instance;
    }

    //Restore default properties and save to file
    private void restoreDefaults(){
        createDefaults();
        properties = defaultProperties;
        save();
    }

    //defines default properties to be used in the event that the user file is deleted or doesn't exist
    private void createDefaults(){
        defaultProperties.setProperty(Property.AUTO_LAUNCH_SERVER.toString(), String.valueOf(false));
        defaultProperties.setProperty(Property.LOCAL_PORT.toString(), String.valueOf(Controller.DEFAULT_PORT));
        defaultProperties.setProperty(Property.MOVIE_FOLDER.toString(), "null");
        defaultProperties.setProperty(Property.PASSWORD.toString(), Controller.DEFAULT_PW);
        defaultProperties.setProperty(Property.REMOTE_ACCESS_ENABLED.toString(), String.valueOf(false));
        defaultProperties.setProperty(Property.REMOTE_PORT.toString(), String.valueOf(Controller.DEFAULT_PORT));
        defaultProperties.setProperty(Property.REQUIRE_AUTHENTICATION.toString(), String.valueOf(false));
        defaultProperties.setProperty(Property.START_ON_LOGIN.toString(), String.valueOf(false));
        defaultProperties.setProperty(Property.USE_DIFFERENT_REMOTE_PORT.toString(), String.valueOf(false));
        defaultProperties.setProperty(Property.USERNAME.toString(), Controller.DEFAULT_USERNAME);
    }

    public boolean configExists(){
        return Files.exists(Path.of(PROPERTY_FILE));
    }

    //Assigns string to specified property
    //Null Strings will be written as "null"
    //Calls save()
    public void setProperty(Property property, String value) {
        if (value == null){
            value = "null";
        }
        properties.setProperty(property.toString(), value);
        save();
    }

    //Assigns boolean to specified property
    public void setProperty(Property property, boolean bool){
        this.setProperty(property, String.valueOf(bool));
    }

    //Assigns int to specifiedproperty
    public void setProperty(Property property, int num){
        this.setProperty(property, String.valueOf(num));
    }

    //returns a string of the specified property
    public String getProperty(Property property){
        load();

        String propertyToReturn = properties.getProperty(property.toString());
        propertyToReturn = propertyToReturn != null? propertyToReturn : defaultProperties.getProperty(property.toString());


        return propertyToReturn;
    }


    //Returns a boolean of the specified property
    public Boolean getPropertyAsBool(Property property){
        return Boolean.valueOf(getProperty(property));
    }

    //Returns an int of the specified property
    public int getPropertyAsInt(Property property){
        int propInt = -1;
        try{
            propInt = Integer.parseInt(getProperty(property));
        }catch (NumberFormatException e){
            System.out.println("Property is not a number");
        }
        return propInt;
    }


    //Save all properties to file
    public void save(){
        try {
            FileOutputStream out = new FileOutputStream(PROPERTY_FILE);
            properties.store(out, "--- HomeStream User Configuration ---");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //reload all data from file. Will restore defaults if file doesn't exist or other errors are found
    public void load() {
        try {
            if (!configExists()){
                restoreDefaults();
            } else {
                FileReader reader = new FileReader(PROPERTY_FILE);
                properties.load(reader);
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            restoreDefaults();
        }
    }
}
