package Controller;

public class Controller {

    private String currentPath;

    public Controller(){
        currentPath = "";
    }


    public void processFileChooserInput(String path){
        currentPath = path;
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
