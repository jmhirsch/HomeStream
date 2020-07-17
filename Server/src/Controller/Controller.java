package Controller;

import Model.Folder;
import ServerService.ServerService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

public class Controller {

    private String currentPath;
    private int portNum;
    private ServerService serverService;
    private Folder root;

    public Controller(){
        currentPath = "";
    }


    public void processFileChooserInput(String path){
        currentPath = path;
        root = new Folder(new File(path), "");
        //root.listAllFolders();
        root.listAllFiles();
    }

    public void startServerService(int portNum, Function<Boolean, Void> callback){
        if (!currentPath.equals("")){ // ensure a folder is actually selected
            this.portNum = portNum;
            serverService = new ServerService(portNum);
            serverService.startServer(this::createContexts);
            callback.apply(true);
        }
    }


    public void stopServerService(Function<Boolean, Void> callback){
        serverService.exit();
        serverService = null;
        callback.apply(false);
    }

    private Void createContexts(HttpServer server){
        server.createContext("/", new MyHandler(root));

        for (Folder folder: root.getFolders()){
            createContexts(server, folder);
        }
        return null;
    }

    private void createContexts(HttpServer server, Folder folder){
        server.createContext("/" + folder.getFile().getName(), new MyHandler(folder));
        for (Folder subFolder: folder.getFolders()){
           createContexts(server, subFolder);
        }
    }

    static class MyHandler implements HttpHandler {


        private final Folder folder;

        public MyHandler(Folder folder){
            this.folder = folder;
        }

         @Override
        public void handle(HttpExchange t) throws IOException {
            JSONObject response = new JSONObject();
            response.put("message", "a message");
            response.put("currentFolder", folder.getFile().getName());
            response.put("path", folder.getPathFromRoot());
            response.put("folders", folder.getJSONTopLevelFolders());
            response.put("files", folder.getJSONFiles());
            System.out.println(response.toString());


            t.sendResponseHeaders(200, response.toString().getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
    }
}
