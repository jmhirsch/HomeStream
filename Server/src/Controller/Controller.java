package Controller;

import Model.Folder;

import java.io.File;

public class Controller {

    private String currentPath;

    public Controller(){
        currentPath = "";
    }


    public void processFileChooserInput(String path){
        currentPath = path;
        Folder root = new Folder(new File(path));
        //root.listAllFolders();
        root.listAllFiles();
    }

    public void startServerService(){
        if (!currentPath.equals("")){ // ensure a folder is actually selected
            // start server
        }

    }

    public void stopServerService(){
        //stop Server
    }
}
