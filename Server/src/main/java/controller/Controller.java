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

    public static final String CACHE_FOLDER_IGNORE_STR = ".Caches";
    public static final String PATH_TO_CACHE_FOLDER = "/" + CACHE_FOLDER_IGNORE_STR;
    public static final int DEFAULT_PORT = 3004;
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PW = "admin";


    // GET
    private static final String GET_DATA_HANDLER_PATH = "/get/all/";
    private static final String GET_FAVORITES_PATH = "/get/favorites/all/";
    private static final String GET_TV_SHOW_PATHS = "/get/tv-show/";
    private static final String GET_MOVIES_PATH = "/get/movies/";
    private static final String GET_MOVIES_FAVORITES_PATH = "/get/movies/favorites";

    //REFRESH (POST)
    private static final String REFRESH_ALL_HANDLER_PATH = "/refresh/all/";
    private static final String REFRESH_MOVIES_PATH = "/refresh/movies/";
    private static final String REFRESH_TV_SHOWS_PATH = "/refresh/tv-shows/";

    //PATCH
    private static final String PATCH_TV_SHOW = "/patch/tv-shows/";
    private static final String PATCH_MOVIE = "/patch/movies/";
    private static final String PATCH_FILE_FOLDER_PATH = "/patch-file-folder/";


    private static final String STREAM_PATH = "/start_stream/";
    private static final String AUTHENTICATE_PATH = "/authenticate/";
    private static final String PING_PATH = "/ping/";


    private final DataController dataController;
    private final JSONServerController serverController;

    private final List<Token> tokens;

    private static final String[] extensionlist = {".mp4", ".m4a", ".m4v", ".f4v", ".fa4", ".m4b", ".m4r", ".f4b", ".mov", ".3gp",
            ".3gp2", ".3g2", ".3gpp", ".3gpp2", ".ogg", ".oga", ".ogv", ".ogx", ".wmv", ".wma",
            ".webm", ".flv", ".avi", ".mpg", ".mkv", ".ts", ".srt"};

    private static final String [] foldersToIngore = {CACHE_FOLDER_IGNORE_STR};
    private String currentMoviePath;
    private String currentTVShowPath;

    public Controller() {
        currentMoviePath = "";
        currentTVShowPath = "";
        tokens = new ArrayList<>();
        dataController = new DataController();
        serverController = new JSONServerController(this);
    }


    public void processFileChooserInput(String moviePath, String tvShowPath) {
        currentMoviePath = moviePath;
        currentTVShowPath = tvShowPath;
        requestDataCreation();
    }

    private void requestDataCreation(){
        dataController.createData(currentMoviePath, currentTVShowPath, extensionlist, foldersToIngore);
    }

    public void startServerService(int portNum, Function<Boolean, Void> callback) {
        if (!currentMoviePath.equals("") && !currentTVShowPath.equals("")) { // ensure a folder is actually selected
            serverController.startServerService(portNum, callback);
        }
    }

    public List<Context> createContextList(){
        List<Context> contextList = new ArrayList<>();
        contextList.add(new Context(REFRESH_ALL_HANDLER_PATH, ServerMethodType.GET, new Notification(REFRESH_ALL_HANDLER_PATH)));
        contextList.add(new Context(GET_DATA_HANDLER_PATH, ServerMethodType.GET, new Notification(GET_DATA_HANDLER_PATH)));
        contextList.add(new Context(PATCH_FILE_FOLDER_PATH, ServerMethodType.PATCH, new Notification(PATCH_FILE_FOLDER_PATH)));
        contextList.add(new Context(STREAM_PATH, ServerMethodType.POST, new Notification(STREAM_PATH)));
        contextList.add(new Context(GET_FAVORITES_PATH, ServerMethodType.GET, new Notification(GET_FAVORITES_PATH)));
        contextList.add(new Context(AUTHENTICATE_PATH, ServerMethodType.POST, new Notification(AUTHENTICATE_PATH)));
        contextList.add(new Context(PING_PATH, ServerMethodType.GET, new Notification(PING_PATH)));
        contextList.add(new Context(GET_TV_SHOW_PATHS, ServerMethodType.GET, new Notification(GET_TV_SHOW_PATHS)));
        contextList.add(new Context(GET_MOVIES_PATH, ServerMethodType.GET, new Notification(GET_MOVIES_PATH)));
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
            if (notification.name().equals(PING_PATH)){
                handlePing(id);
            }
            System.out.println("Received notification " + notification.name());
            if (notification.name().equals(AUTHENTICATE_PATH)){
                System.out.println("Authenticating");
            }
            else if (!authenticate(obj, id)){
                System.out.println("Not authenticated");
                return;
            }else{
                System.out.println("Authenticated");
            }
            switch (notification.name()) {
                case STREAM_PATH -> handleFileStreamingRequested(obj, id);
                case GET_DATA_HANDLER_PATH -> handleGetData(id);
                case PATCH_FILE_FOLDER_PATH -> handlePatchWithHash(obj, id);
                case REFRESH_ALL_HANDLER_PATH -> handleRefresh(id);
                case GET_FAVORITES_PATH -> handleGetFavorites(id);
                case AUTHENTICATE_PATH -> handleAuthentication(obj, id);
                case GET_TV_SHOW_PATHS -> handleGetTVShow(id);
                case GET_MOVIES_PATH -> handleGetMovies(id);
                default -> System.out.println("Incorrecto notification: " + notification.name() + " " + obj.toString() + " " + id);
            }
        }catch (Exception e){
            System.out.println("Error getting notification " + notification.name() + " msg: " + e.getLocalizedMessage());
        }
    }

    private void handleGetMovies(long id) {
        JSONObject response = new JSONObject();
        response.put("message", "movies transfered successfully!");
        response.put("folders", dataController.getMovies());
        System.out.println(response.toString(5));
        serverController.handleRequestResponse(id, response, 200);
    }

    private void handleGetTVShow(long id) {
        JSONObject response = new JSONObject();
        //response.put("data", dataController.getTVShowData());
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
                updated = dataController.updateMovieFileAtHash(hash, isFavorite, requestObject.getInt("playbackPosition"));
            }else if (type.equalsIgnoreCase(FileType.FOLDER.toString())){
                updated = dataController.updateMovieFolderAtHash(hash, isFavorite);
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