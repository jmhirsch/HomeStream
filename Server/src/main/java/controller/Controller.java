package controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import model.CFile;
import model.Folder;
import org.json.JSONObject;
import services.ServerService;
import services.StreamingService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.function.Function;

public class Controller {

    public static final String CACHE_FOLDER_IGNORE_STR = ".Caches";
    public static final String PATH_TO_CACHE_FOLDER = "/" + CACHE_FOLDER_IGNORE_STR;


    public static final int DEFAULT_PORT = 3004;
    public static final String DEFAULT_USERNAME = "admin";
    public static final String DEFAULT_PW = "admin";
    public static final String GET_DATA_HANDLER_PATH = "/get-data/";
    public static final String REFRESH_HANDLER_PATH = "/Refresh/";


    private String currentPath;
    private int portNum;
    private ServerService serverService;
    private Folder root;
    private double secureKey;

    public Controller() {
        currentPath = "";
    }


    public void processFileChooserInput(String path) {
        currentPath = path;
        root = new Folder(new File(path));
        System.out.println("root path: " + root.getFile().getPath());
        //System.out.println(root.getJSONItems().toString());
    }

    public void startServerService(int portNum, Function<Boolean, Void> callback) {
        if (!currentPath.equals("")) { // ensure a folder is actually selected
            this.portNum = portNum;
            serverService = ServerService.getInstance();
            secureKey = serverService.startServer(this::createContexts, portNum);
            callback.apply(true);
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

    private void removeContexts(Folder folder) {
        for (CFile file : folder.getFiles()) {
            ServerService.getInstance().removeContext(secureKey, "/" + file.getHash());
        }

        for (Folder subFolder : folder.getFolders()) {
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
        return null;
    }

    private void createContexts(Folder folder) {
        if (folder.getName().equals(CACHE_FOLDER_IGNORE_STR)) {
            System.out.println("ignoring cache folder");
            return;
        }

        for (CFile file : folder.getFiles()) {
            ServerService.getInstance().addContext(secureKey, "/" + file.getHash(), new FileHandler(file, secureKey));
            System.out.println("/" + file.getHash());
        }

        for (Folder subFolder : folder.getFolders()) {
            createContexts(subFolder);
        }
    }

    static class FileHandler implements HttpHandler {

        private final CFile file;
        private final double secureKey;

        public FileHandler(CFile file, double secureKey) {
            this.file = file;
            this.secureKey = secureKey;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            JSONObject response = file.getJSONFile();
            System.out.println(response);
            t.sendResponseHeaders(200, response.toString().getBytes().length);
            new StreamingService(secureKey, file);
            OutputStream os = t.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
    }

    static class FolderHandler implements HttpHandler {
        private final Folder folder;

        public FolderHandler(Folder folder) {
            this.folder = folder;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            Headers h = t.getRequestHeaders();
            InputStream r = t.getRequestBody();

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
}
