package controller;

import com.flynnbuc.httpserverwrapper.enums.ServerMethodType;
import com.flynnbuc.httpserverwrapper.exceptions.CouldNotFindIPException;
import com.flynnbuc.httpserverwrapper.interfaces.ContextManager;
import com.flynnbuc.httpserverwrapper.main.JSONServerController;
import com.flynnbuc.httpserverwrapper.model.Context;
import com.flynnbuc.httpserverwrapper.model.Notification;
import com.flynnbuc.httpserverwrapper.services.IPAddressChecker;
import enums.FileType;
import enums.Property;
import model.network.NetworkFile;
import model.network.Token;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import services.PropertyService;
import services.StreamingService;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;


public class Controller implements ContextManager {

    private static final String STREAM_NOTIFICATION = "Stream Request Received";
    private static final String GET_NOTIFICATION = "Get Data Request Received";
    private static final String UPDATE_MODEL = "Update Folders and Files Received";
    private static final String REFRESH_NOTIFICATION = "Refresh Notification Received";
    private static final String FAVORITE_NOTIFICATION = "Get Favorites";
    private static final String AUTHENTICATION_NOTIFICATION = "Authenticate";
    private static final String GET_TV_SHOW_NOTIFICATION = "Get TV Shows";
    private static final String PING = "ping";

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
    private static final String GET_FAVORITES_PATH = "/Get_favorites/";
    private static final String AUTHENTICATE_PATH = "/Authenticate/";
    private static final String PING_PATH = "/ping/";
    private static final String GET_TV_SHOW_PATHS = "/get-tv-show/";

    private final DataController dataController;
    private final JSONServerController serverController;

    private final List<Token> tokens;

    private static final String[] extensionlist = {".mp4", ".m4a", ".m4v", ".f4v", ".fa4", ".m4b", ".m4r", ".f4b", ".mov", ".3gp",
            ".3gp2", ".3g2", ".3gpp", ".3gpp2", ".ogg", ".oga", ".ogv", ".ogx", ".wmv", ".wma",
            ".webm", ".flv", ".avi", ".mpg", ".mkv", ".ts", ".srt"};

    private static final String [] foldersToIngore = {CACHE_FOLDER_IGNORE_STR};
    private String currentPath;

    public Controller() {
        currentPath = "";
        tokens = new ArrayList<>();
        dataController = new DataController();
        serverController = new JSONServerController(this);
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
        contextList.add(new Context(GET_FAVORITES_PATH, ServerMethodType.GET, new Notification(FAVORITE_NOTIFICATION)));
        contextList.add(new Context(AUTHENTICATE_PATH, ServerMethodType.POST, new Notification(AUTHENTICATION_NOTIFICATION)));
        contextList.add(new Context(PING_PATH, ServerMethodType.GET, new Notification(PING)));
        contextList.add(new Context(GET_TV_SHOW_PATHS, ServerMethodType.GET, new Notification(GET_TV_SHOW_NOTIFICATION)));
        return contextList;
    }

    public void stopServerService(Function<Boolean, Void> callback) {
        serverController.stopServerService(callback);
    }

    private boolean authenticate(Object obj, long id){
        if (PropertyService.instance.getPropertyAsBool(Property.REQUIRE_AUTHENTICATION)){
            if (obj instanceof JSONObject object){
                if (object.has("headers")) {
                    JSONObject headers = new JSONObject(object.get("headers").toString());
                    if (headers.has("Token")) {
                        JSONArray tokenArray = new JSONArray(new JSONTokener(headers.get("Token").toString()));
                       if (tokenArray.get(0) instanceof String tokenStr)
                        for (Token token: tokens){
                            if (token.getToken().equals(tokenStr)){
                                return true;
                            }
                        }
                    }
                }
            }

            JSONObject response = new JSONObject();
            response.put("message", "Not authenticated");
            serverController.handleRequestResponse(id, response, 401); // not authorized
            return false;
        }
        return true;
    }

    @Override
    public void notificationReceived(Notification notification, Object obj, long id) {
        try {
            if (notification.name().equals(PING)){
                handlePing(id);
            }
            System.out.println("Received notification " + notification.name());
            if (notification.name().equals(AUTHENTICATION_NOTIFICATION)){
                System.out.println("Authenticating");
            }
            else if (!authenticate(obj, id)){
                System.out.println("Not authenticated");
                return;
            }else{
                System.out.println("Authenticated");
            }
            switch (notification.name()) {
                case STREAM_NOTIFICATION -> handleFileStreamingRequested(obj, id);
                case GET_NOTIFICATION -> handleGetData(id);
                case UPDATE_MODEL -> handlePatchWithHash(obj, id);
                case REFRESH_NOTIFICATION -> handleRefresh(id);
                case FAVORITE_NOTIFICATION -> handleGetFavorites(id);
                case AUTHENTICATION_NOTIFICATION -> handleAuthentication(obj, id);
                case GET_TV_SHOW_NOTIFICATION -> handleGetTVShow(id);
                default -> System.out.println("Incorrecto notification: " + notification.name() + " " + obj.toString() + " " + id);
            }
        }catch (Exception e){
            System.out.println("Error getting notification " + notification.name() + " msg: " + e.getLocalizedMessage());
        }
    }

    private void handleGetTVShow(long id) {
        JSONObject response = new JSONObject();
        response.put("data", dataController.getTVShowData());
        serverController.handleRequestResponse(id, response, 200);
    }

    private void handlePing(long id) {
        JSONObject response = new JSONObject();
        response.put("message", "success");
        serverController.handleRequestResponse(id, response, 200);
    }

    private void handleAuthentication(Object obj, long id) {
        JSONObject response = new JSONObject();
        if (obj instanceof JSONObject request) {
            if (PropertyService.instance.getPropertyAsBool(Property.REQUIRE_AUTHENTICATION)) {
                String username = request.getString("username");
                String password = request.getString("password");
                String expectedUsername = PropertyService.instance.getProperty(Property.USERNAME);
                String expectedPassword = PropertyService.instance.getProperty(Property.PASSWORD);
                if (!username.equals(expectedUsername) || !password.equals(expectedPassword)){
                    response.put("message", "Incorrect Login");
                    serverController.handleRequestResponse(id, response, 401); // invalid login
                    return;
                }
            }
            Token token = new Token();
            tokens.add(token);
            response.put("token", token.getToken());
            if (true){//PropertyService.getInstance().getPropertyAsBool(Property.REMOTE_ACCESS_ENABLED)) {
                try {
                    response.put("ip", IPAddressChecker.getRemoteIP());
                } catch (CouldNotFindIPException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(response.toString(4));
            serverController.handleRequestResponse(id, response, 200);
        }
    }

    private void handleGetFavorites(long id) {
        JSONObject response = new JSONObject();
        response.put("message", "Favorites");
        response.put("folders", dataController.getJSONDataFavorites());
        serverController.handleRequestResponse(id, response,200);
    }

    public void handleFileStreamingRequested(Object obj, long id) throws Exception {
        try {
            int responseCode = -1;
            JSONObject response = new JSONObject();

            if (obj instanceof JSONObject requestObject) {
                String address = requestObject.getString("address");
                long hash = requestObject.getLong("hash");
                NetworkFile file = dataController.getFile(hash);
                if (file != null) {
                    StreamingService ss = new StreamingService(file, address);
                    ss.addPropertyChangeListnener(serverController);
                    ss.createContexts();
                    response = file.getData();
                    responseCode = 200;
                } else {
                    responseCode = 406;
                    response.put("message", "File with hash " + hash + "not found. Streaming not started");
                }
            } else {
                responseCode = 400;
                response.put("message", "Invalid data format. Streaming not started");
            }
            serverController.handleRequestResponse(id, response, responseCode);
        }catch (Exception e){
            throw new Exception(e);
        }
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