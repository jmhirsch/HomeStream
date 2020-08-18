package controller;

import enums.FileType;
import enums.ServerMethodType;
import model.Context;
import model.NetworkFile;
import model.requests.Notification;
import org.json.JSONObject;
import services.StreamingService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/*
Defines
 */

public class Controller extends ControllerManager {

    private static final String STREAM_NOTIFICATION = "Stream Request Received";
    private static final String GET_NOTIFICATION = "Get Data Request Received";
    private static final String UPDATE_MODEL = "Update Folders and Files Received";
    private static final String REFRESH_NOTIFICATION = "Refresh Notification Received";

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
        contextList.add(new Context(REFRESH_HANDLER_PATH, ServerMethodType.GET, new Notification(REFRESH_NOTIFICATION)));
        contextList.add(new Context(GET_DATA_HANDLER_PATH, ServerMethodType.GET, new Notification(GET_NOTIFICATION)));
        contextList.add(new Context(PATCH_FILE_FOLDER_PATH, ServerMethodType.PATCH, new Notification(UPDATE_MODEL)));
        contextList.add(new Context(STREAM_PATH, ServerMethodType.POST, new Notification(STREAM_NOTIFICATION)));
        return contextList;
    }

    public void stopServerService(Function<Boolean, Void> callback) {
        serverController.stopServerService(callback);
    }

    @Override
    public void notificationReceived(Notification notification, Object obj, long id) {

        switch (notification.name()){
            case STREAM_NOTIFICATION ->handleFileStreamingRequested(obj, id);
            case GET_NOTIFICATION -> handleGetData(id);
            case UPDATE_MODEL -> handlePatchWithHash(obj, id);
            case REFRESH_NOTIFICATION -> handleRefresh(id);
            default -> System.out.println("Incorrecto notification: " + notification.name() + " " +  obj.toString() + " " + id);
        }
    }

    public void handleFileStreamingRequested(Object obj, long id){
        int responseCode = -1;
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
                responseCode = 200;
            }else{
                responseCode = 406;
                response.put("message", "File with hash " + hash + "not found. Streaming not started");
            }
        }else{
            responseCode = 400;
            response.put("message", "Invalid data format. Streaming not started");
        }
        serverController.handleRequestResponse(id, response, responseCode);
    }

    public void handlePatchWithHash(Object obj, long id){
        int responseCode = -1;
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
                responseCode = 200;
                response.put("message", "object at " + hash + " updated");
            }else{
                responseCode = 404;
                response.put("message", "object with " + hash + "not found");
            }
        }else{
            responseCode = 400;
            response.put("message", "invalid data update patch request");
        }
        serverController.handleRequestResponse(id, response, responseCode);
    }

    public void handleGetData(long id){
        JSONObject response = new JSONObject();
        response.put("message", "Returned Data from Network");
        response.put("folders", dataController.getJSONData());
        serverController.handleRequestResponse(id, response,200);
    }

    public void handleRefresh(long id){
        for (Context context: createContextList()){
            System.out.println("Removing context at path: " + context.path());
            serverController.removeContext(context.path());
        }

        requestDataCreation();

        serverController.createContexts(createContextList().toArray(new Context[]{}));
        JSONObject response = new JSONObject();
        response.put("message", "Refresh Complete!");
        System.out.println(response.toString());
        serverController.handleRequestResponse(id, response, 200);

    }

    @Override
    public void requestData() {
        serverController.createContexts(createContextList().toArray(new Context[]{}));
    }
}
