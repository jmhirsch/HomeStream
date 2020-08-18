package controller;

import enums.ContextType;
import enums.FileType;
import enums.Notification;
import model.Context;
import model.NetworkFile;
import model.NetworkFolder;
import org.json.JSONObject;
import services.StreamingService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/*
Defines
 */

public class Controller extends ControllerManager {


    public static final String CACHE_FOLDER_IGNORE_STR = ".Caches";
    public static final String PATH_TO_CACHE_FOLDER = "/" + CACHE_FOLDER_IGNORE_STR;
    public static final int DEFAULT_PORT = 3004;
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PW = "admin";

    private static final String PLAY_FILE_PATH = "/Play/";
    private static final String PATCH_FILE_FOLDER_PATH = "/Patch-file-folder/";
    private static final String GET_DATA_HANDLER_PATH = "/get-data/";
    private static final String REFRESH_HANDLER_PATH = "/Refresh/";
    private static final String STREAM_PATH = "/start_stream/";

    private final DataController dataController;
    private final ServerController serverController;

    private static final String[] extensionlist = {".mp4", ".m4a", ".m4v", ".f4v", ".fa4", ".m4b", ".m4r", ".f4b", ".mov", ".3gp",
            ".3gp2", ".3g2", ".3gpp", ".3gpp2", ".ogg", ".oga", ".ogv", ".ogx", ".wmv", ".wma",
            ".webm", ".flv", ".avi", ".mpg", ".mkv", ".ts", ".srt"};

    private static final String [] foldersToIngore = {CACHE_FOLDER_IGNORE_STR};


    private String currentPath;
    private int portNum;

    private NetworkFolder root;

    public Controller() {
        currentPath = "";
        dataController = new DataController(this);
        serverController = new ServerController(this);
        serverController.setNotificationListener(this);
    }


    public void processFileChooserInput(String path) {
        currentPath = path;
        requestDataCreation();
    }

    private void requestDataCreation(){
        dataController.createData(currentPath, extensionlist, foldersToIngore);
    }

    public void startServerService(int portNum, Function<Boolean, Void> callback) {
        if (!currentPath.equals("")) { // ensure a folder is actually selected
            serverController.startServerService(portNum, callback);
        }
    }

    public List<Context> createContextList(){
        List<Context> contextList = new ArrayList<>();

        contextList.add(new Context(REFRESH_HANDLER_PATH, ContextType.REFRESH_CONTEXT));
        contextList.add(new Context(GET_DATA_HANDLER_PATH, ContextType.GET_DATA_CONTEXT));
        contextList.add(new Context(PATCH_FILE_FOLDER_PATH, ContextType.PATCH_FILE_FOLDER));
        contextList.add(new Context(STREAM_PATH, ContextType.STREAMING_START_CONTEXT));
        return contextList;
    }

    public void stopServerService(Function<Boolean, Void> callback) {
        serverController.stopServerService(callback);
    }

    @Override
    public void notificationReceived(Notification notification, Object obj, long id) {
        switch (notification){
            case REFRESH_CALLED -> handleRefresh(id);
            case PATCH_WITH_HASH -> handlePatchWithHash(obj, id);
            case FILE_STREAMING_REQUESTED -> handleFileStreamingRequested(obj, id);
            case GET_CALLED -> handleGetData(id);
        }
    }

    public void handleFileStreamingRequested(Object obj, long id){
        boolean requestProcessed = false;
        JSONObject response = new JSONObject();

        if (obj instanceof JSONObject requestObject){
            String address = requestObject.getString("address");
            long hash = requestObject.getLong("hash");
            NetworkFile file = dataController.getFile(hash);
            if (file != null) {
                StreamingService ss = new StreamingService(file, address);
                ss.addObserver(serverController);
                ss.createContexts();
                response = file.getJSONFile();
                requestProcessed = true;
            }else{
                response.put("message", "File with hash " + hash + "not found. Streaming not started");
            }
        }else{
            response.put("message", "Invalid data format. Streaming not started");
        }
        serverController.handleRequestResponse(id, response, requestProcessed);
    }

    public void handlePatchWithHash(Object obj, long id){
        boolean updated = false;
        JSONObject response = new JSONObject();
        if (obj instanceof JSONObject requestObject){

            String type = requestObject.getString("type");
            long hash = requestObject.getLong("hash");
            boolean isFavorite = requestObject.getBoolean("isFavorite");

            if (type.equalsIgnoreCase(FileType.FILE.toString())){
                updated = dataController.updateFileAtHash(hash, isFavorite, requestObject.getInt("playbackPosition"));
            }else if (type.equalsIgnoreCase(FileType.FOLDER.toString())){
                updated = dataController.updateFolderAtHash(hash, isFavorite);
            }
            if (updated){
                response.put("message", "object at " + hash + " updated");
            }else{
                response.put("message", "object with " + hash + "not found");
            }
        }else{
            response.put("message", "invalid data update patch request");
        }
        serverController.handleRequestResponse(id, response, updated);
    }

    public void handleGetData(long id){
        JSONObject response = new JSONObject();
        response.put("message", "Returned Data from Network");
        response.put("folders", dataController.getJSONData());
        serverController.handleRequestResponse(id, response,true);
    }

    public void handleRefresh(long id){
        for (Context context: createContextList()){
            System.out.println("Removing context at path: " + context.getPath());
            serverController.removeContext(context.getPath());
        }

        requestDataCreation();

        serverController.createContexts(createContextList());
        JSONObject response = new JSONObject();
        response.put("message", "Refresh Complete!");
        System.out.println(response.toString());
        serverController.handleRequestResponse(id, response, true);

    }

    @Override
    public void requestData() {
        serverController.createContexts(createContextList());
    }

}
