package Controller;

import Model.Folder;
import ServerService.ServerService;
import View.UI;

import javax.swing.*;
import java.io.File;
import java.util.function.Function;

public class Controller {

    private String currentPath;
    private int portNum;
    private ServerService serverService;

    public Controller(){
        currentPath = "";
    }


    public void processFileChooserInput(String path){
        currentPath = path;
        Folder root = new Folder(new File(path));
        //root.listAllFolders();
        root.listAllFiles();
    }

    public void startServerService(int portNum, Function<Boolean, Void> callback){
        if (!currentPath.equals("")){ // ensure a folder is actually selected
            this.portNum = portNum;
            serverService = new ServerService(portNum);
            serverService.startServer(this::displayMessage);
            callback.apply(true);
        }
    }



    public void stopServerService(Function<Boolean, Void> callback){
        serverService.exit();
        serverService = null;
        callback.apply(false);
    }

    private Void displayMessage(String message){
        System.out.println(message);

        return null;
    }
}
