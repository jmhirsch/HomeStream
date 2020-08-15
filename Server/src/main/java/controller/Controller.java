package controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.NetworkFile;
import model.NetworkFolder;
import org.json.JSONObject;
import org.json.JSONTokener;
import services.ServerService;
import services.StreamingService;

import java.io.*;
import java.net.URI;
import java.util.function.Function;
import java.util.stream.Collectors;

/*
Defines
 */

public class Controller {

    public static final String CACHE_FOLDER_IGNORE_STR = ".Caches";
    public static final String PATH_TO_CACHE_FOLDER = "/" + CACHE_FOLDER_IGNORE_STR;


    public static final int DEFAULT_PORT = 3004;
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PW = "admin";
    public static final String GET_DATA_HANDLER_PATH = "/get-data/";
    public static final String REFRESH_HANDLER_PATH = "/Refresh/";

    private static final String[] extensionlist = {".mp4", ".m4a", ".m4v", ".f4v", ".fa4", ".m4b", ".m4r", ".f4b", ".mov", ".3gp",
            ".3gp2", ".3g2", ".3gpp", ".3gpp2", ".ogg", ".oga", ".ogv", ".ogx", ".wmv", ".wma",
            ".webm", ".flv", ".avi", ".mpg", ".mkv", ".ts", ".srt"};


    private String currentPath;
    private int portNum;
    private ServerService serverService;
    private NetworkFolder root;
    private double secureKey;

    public Controller() {
        currentPath = "";
    }


    public void processFileChooserInput(String path) {
        currentPath = path;
        root = new NetworkFolder(new File(path), extensionlist, new String[]{CACHE_FOLDER_IGNORE_STR});
        System.out.println("root path: " + root.getFile().getPath());
    }

    public void startServerService(int portNum, Function<Boolean, Void> callback) {
        if (!currentPath.equals("")) { // ensure a folder is actually selected
            this.portNum = portNum;
            serverService = ServerService.getInstance();
            boolean created = serverService.startServer(this::createContexts, portNum);
            callback.apply(created);
        }
    }

    private Void refresh(Void none) {
        removeContexts(root);
        System.out.println("removed contexts");
        processFileChooserInput(root.getFile().getPath());
        ServerService.getInstance().removeContext(secureKey, GET_DATA_HANDLER_PATH);
        ServerService.getInstance().removeContext(secureKey, REFRESH_HANDLER_PATH);
        createContexts(secureKey);
        System.out.println("Readded contexts");
        return null;
    }

    private void removeContexts(NetworkFolder folder) {
        for (NetworkFile file : folder.getFiles()) {
            ServerService.getInstance().removeContext(secureKey, "/" + file.getHash());
        }
        for (NetworkFolder subFolder : folder.getFolders()) {
            removeContexts(subFolder);
        }
    }


    public void stopServerService(Function<Boolean, Void> callback) {
        serverService.exit(secureKey);
        serverService = null;
        callback.apply(false);
    }

    private Void createContexts(Double secureKey) {
        this.secureKey = secureKey;
        createContexts(root);
        ServerService.getInstance().addContext(secureKey, REFRESH_HANDLER_PATH, new RefreshHandler(this::refresh));
        ServerService.getInstance().addContext(secureKey, GET_DATA_HANDLER_PATH, new FolderHandler(root));
        ServerService.getInstance().addContext(secureKey, "/POST/", new PostHandler(this::JSONResponse));
        return null;
    }

    private void createContexts(NetworkFolder folder) {
        if (folder.getName().equals(CACHE_FOLDER_IGNORE_STR)) {
            System.out.println("ignoring cache folder");
            return;
        }

        for (NetworkFile file : folder.getFiles()) {
            ServerService.getInstance().addContext(secureKey, "/" + file.getHash(), new FileHandler(file, secureKey));
        }

        for (NetworkFolder subFolder : folder.getFolders()) {
            createContexts(subFolder);
        }
    }

    private boolean JSONResponse(JSONObject object){

        System.out.println(object.get("Type"));

        if (object.get("Type").equals("File")){
            System.out.println(object.get("hash"));
            NetworkFile file = root.findFile(object.getLong("hash"));
            System.out.println(file == null);
            if (file != null){
                file.setFavorite(object.getBoolean("isFavorite"));
            return true;
             }
        }

        return false;
    }

    static class FileHandler implements HttpHandler {
        private final NetworkFile file;
        private final double secureKey;

        public FileHandler(NetworkFile file, double secureKey) {
            this.file = file;
            this.secureKey = secureKey;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {

            System.out.println(file.getName());
            if (t.getRequestMethod().equalsIgnoreCase("POST")) {
                System.out.println("Here");
                InputStream is = t.getRequestBody();
                String request = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
                JSONTokener parser = new JSONTokener(request);
                JSONObject jsonObject = new JSONObject(parser);
                String worked = "false";
                int responseCode = 400;
                if (jsonObject.getLong("hash") == file.getHash()){
                    file.setFavorite(jsonObject.getBoolean("isFavorite"));
                    System.out.println("File " + file.getName() + " updated -- favorite:" + file.isFavorite());
                    worked = "true";
                    responseCode = 200;
                }

                JSONObject response = new JSONObject();
                response.put("message", "file " + file.getName() + "updated?:" + worked);
                t.sendResponseHeaders(responseCode, response.toString().getBytes().length);
                OutputStream os  = t.getResponseBody();
                os.write(response.toString().getBytes());
                os.close();

                System.out.println(response.toString());
            } else {

                JSONObject response = file.getJSONFile();
                System.out.println(response);
                t.sendResponseHeaders(200, response.toString().getBytes().length);
                new StreamingService(secureKey, file);
                OutputStream os = t.getResponseBody();
                os.write(response.toString().getBytes());
                os.close();
            }
        }
    }

    static class FolderHandler implements HttpHandler {
        private final NetworkFolder folder;

        public FolderHandler(NetworkFolder folder) {
            this.folder = folder;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers h = t.getRequestHeaders();
            InputStream r = t.getRequestBody();

            System.out.println("Protocol: " + t.getProtocol() + "Request Method: " + t.getRequestMethod());
            System.out.println("Headers::::");
            System.out.println(h.entrySet());
            System.out.println("Request Body");
            System.out.println(r.readAllBytes());

            URI a = t.getRequestURI();

            JSONObject response = new JSONObject();
            int responseCode = 0;

            System.out.println(a.getPath());
            if (a.getPath().equals(GET_DATA_HANDLER_PATH) || a.getPath().equals(GET_DATA_HANDLER_PATH + "/")) {
                System.out.println("Valid Request");
                response.put("message", "a message");
                response.put("currentFolder", folder.getFile().getName());
                response.put("path", folder.getPathFromRoot());
                response.put("folders", folder.getJSONItems());
                responseCode = 200;

            } else {
                System.out.println("Invalid Request");
                response.put("message", "Invalid Query");
                responseCode = 404;
            }

            t.sendResponseHeaders(responseCode, response.toString().getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }

    }

    static class RefreshHandler implements HttpHandler {

        private Function<Void, Void> refreshCallback;

        public RefreshHandler(Function<Void, Void> refreshCallback) {
            this.refreshCallback = refreshCallback;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            JSONObject response = new JSONObject();
            response.put("message", "Refreshed!");
            System.out.println(response.toString());
            refreshCallback.apply(null);
            t.sendResponseHeaders(200, response.toString().getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
    }

    static class PostHandler implements HttpHandler{

        private Function<JSONObject, Boolean> callback;

        public PostHandler(Function<JSONObject, Boolean> callback){
            this.callback = callback;
        }

        public void handle(HttpExchange t) throws IOException {
        }
    }
}
