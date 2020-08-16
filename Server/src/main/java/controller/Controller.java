package controller;

import enums.Notification;
import interfaces.NotificationListener;
import model.NetworkFile;
import model.NetworkFolder;
import org.json.JSONObject;
import services.ServerService;

import java.io.*;
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

    private final DataController dataController;
    private final ServerController serverController;

    private static final String[] extensionlist = {".mp4", ".m4a", ".m4v", ".f4v", ".fa4", ".m4b", ".m4r", ".f4b", ".mov", ".3gp",
            ".3gp2", ".3g2", ".3gpp", ".3gpp2", ".ogg", ".oga", ".ogv", ".ogx", ".wmv", ".wma",
            ".webm", ".flv", ".avi", ".mpg", ".mkv", ".ts", ".srt"};


    private String currentPath;
    private int portNum;

    private NetworkFolder root;

    public Controller() {
        currentPath = "";
        dataController = new DataController(this);
        serverController = new ServerController(CACHE_FOLDER_IGNORE_STR, this);
        serverController.setNotificationListener(this);
        serverController.addObserver(dataController);
    }


    public void processFileChooserInput(String path) {
        currentPath = path;
        root = dataController.createData(path);
    }

    public void startServerService(int portNum, Function<Boolean, Void> callback) {
        if (!currentPath.equals("")) { // ensure a folder is actually selected

            serverController.startServerService(portNum, callback);
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

    public void stopServerService(Function<Boolean, Void> callback) {
        serverController.stopServerService(callback);
    }

    @Override
    public void NotificationReceived(Notification notification, Object obj) {

    }

//    private void removeContexts(NetworkFolder folder) {
//        for (NetworkFile file : folder.getFiles()) {
//            ServerService.getInstance().removeContext(secureKey, "/" + file.getHash());
//        }
//        for (NetworkFolder subFolder : folder.getFolders()) {
//            removeContexts(subFolder);
//        }
//    }






//    private boolean JSONResponse(JSONObject object){
//
//        System.out.println(object.get("Type"));
//
//        if (object.get("Type").equals("File")){
//            System.out.println(object.get("hash"));
//            NetworkFile file = root.findFile(object.getLong("hash"));
//            System.out.println(file == null);
//            if (file != null){
//                file.setFavorite(object.getBoolean("isFavorite"));
//            return true;
//             }
//        }
//
//        return false;
//    }
}
