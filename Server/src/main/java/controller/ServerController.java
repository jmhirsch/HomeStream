package controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import interfaces.NotificationListener;
import model.Context;
import model.NetworkFile;
import model.NetworkFolder;
import observer.Subject;
import org.json.JSONObject;
import org.json.JSONTokener;
import services.ServerService;
import services.StreamingService;

import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class ServerController extends Subject {

    public static final String GET_DATA_HANDLER_PATH = "/get-data/";
    public static final String REFRESH_HANDLER_PATH = "/Refresh/";
    private final String cacheFolderToIgnore;

    private ServerService serverService;
    private final ControllerManager manager;
    private NotificationListener notificationListener;
    private int portNum;

    private double secureKey;

    public ServerController(String cacheFolderToIgnore, ControllerManager manager){
        this.cacheFolderToIgnore = cacheFolderToIgnore;
        this.manager = manager;
    }

    public void setNotificationListener(NotificationListener notificationListener){
        this.notificationListener = notificationListener;
    }

    public void startServerService(int portNum, Function<Boolean, Void> callback) {
            this.portNum = portNum;
            serverService = ServerService.getInstance();
            boolean created = serverService.startServer(this::createContexts, portNum);
            callback.apply(created);
    }

    public void stopServerService(Function<Boolean, Void> callback) {
        serverService.exit(secureKey);
        serverService = null;
        callback.apply(false);
    }

    private Void createContexts(Double secureKey, List<Context> contextsToCreate) {
        this.secureKey = secureKey;
        createContexts(root);

        for (Context context: contextsToCreate){
            switch (context.getType()){
                case FILE_CONTEXT: break;
                case FOLDER_CONTEXT: break;
                case REFRESH_CONTEXT: break;
                case GET_DATA_CONTEXT: break;
                default: break;
            }
        }
        return null;
    }

    private void createFileContext(String path){

    }

    private void createFolderContext(String path){

    }

    private void createRefreshContext(String path){
        ServerService.getInstance().addContext(secureKey, REFRESH_HANDLER_PATH, new RefreshHandler(this::refresh));
    }

    private void createGetDataContext(String path){
        ServerService.getInstance().addContext(secureKey, GET_DATA_HANDLER_PATH, new FolderHandler(root));
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

    private void createContexts(NetworkFolder folder) {
        if (folder.getName().equals(cacheFolderToIgnore)) {
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

    static class FileHandler implements HttpHandler {
        private final NetworkFile file;
        private final double secureKey;

        public FileHandler(NetworkFile file, double secureKey) {
            this.file = file;
            this.secureKey = secureKey;
        }

        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            String request = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            JSONTokener parser = new JSONTokener(request);
            JSONObject jsonObject = new JSONObject(parser);

            System.out.println("URLLLLLLL:  " + t.getRequestHeaders());
            System.out.println(file.getName());
            if (t.getRequestMethod().equalsIgnoreCase("PATCH")) {
                String worked = "false";
                int responseCode = 400;
                if (jsonObject.getLong("hash") == file.getHash()){
                    file.setFavorite(jsonObject.getBoolean("isFavorite"));
                    file.setCurrentPlaybackPosition(jsonObject.getInt("playbackPosition"));
                    System.out.println("File " + file.getName() + " updated -- favorite:" + file.isFavorite()  + " P.P: " + jsonObject.get("playbackPosition"));
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
                String addressToUse = jsonObject.getString("address");
                System.out.println("address to use in service: " + addressToUse);
                JSONObject response = file.getJSONFile();
                System.out.println(response);
                t.sendResponseHeaders(200, response.toString().getBytes().length);
                new StreamingService(secureKey, file, addressToUse);
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
}
