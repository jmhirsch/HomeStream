package Controller;

import Model.Folder;
import View.UI;

import java.io.File;
import java.util.function.Function;

public class Controller {

    private String currentPath;
    private int portNum;

    public Controller(){
        currentPath = "";
    }


    public void processFileChooserInput(String path){
        currentPath = path;
        Folder root = new Folder(new File(path));
        //root.listAllFolders();
        root.listAllFiles();
    }

    public void startServerService(int portNum, Function<Boolean, Boolean> callback){
        if (!currentPath.equals("")){ // ensure a folder is actually selected
            this.portNum = portNum;
            //start server

        }
        callback.apply(true);
    }

    public void stopServerService(Function<Boolean, Boolean> callback){
        //stop Server
        callback.apply(false);
    }
}
