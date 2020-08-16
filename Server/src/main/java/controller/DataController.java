package controller;

import interfaces.NotificationListener;
import model.NetworkFile;
import model.NetworkFolder;
import observer.Observer;
import observer.Subject;
import org.json.JSONObject;
import services.UserDataService;

import java.io.File;
import java.util.HashMap;

public final class DataController extends Subject implements Observer {


    private final NotificationListener notificationListener;

    public DataController(NotificationListener notificationListener){
        this.notificationListener = notificationListener;
    }

    @Override
    public void update() {

    }

    @Override
    public void setSubject(Subject subject) {

    }

    public NetworkFolder createData(String path) {
        currentPath = path;
        root = new NetworkFolder(new File(path), extensionlist, new String[]{CACHE_FOLDER_IGNORE_STR});
        System.out.println("root path: " + root.getFile().getPath());
    }



//    private void buildHashmap(){
//        HashMap<Long, JSONObject> map = new HashMap<>();
//        buildHashmap(map, root);
//
//        UserDataService service = new UserDataService();
//        service.write(map);
//    }
//
//    private void buildHashmap(HashMap<Long, JSONObject> map, NetworkFolder folder){
//        map.put(folder.getHash(), folder.getData());
//
//        for (NetworkFile file: folder.getFiles()){
//            map.put(file.getHash(), file.getData());
//        }
//
//        for (NetworkFolder subfolder: folder.getFolders()){
//            buildHashmap(map, subfolder);
//        }
//    }
}
