package Controller;

import Model.Folder;
import ServerService.ServerService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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

    public Void createContexts(HttpServer server){
        server.createContext("/", new MyHandler(root));

        for (Folder folder: root.getFolders()){
            server.createContext("/" + folder.getFile().getName(), new MyHandler(folder));
        }
        return null;
    }

    static class MyHandler implements HttpHandler {


        private final Folder folder;

        public MyHandler(Folder folder){
            this.folder = folder;
        }

         @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();

            JSONObject response = new JSONObject();
            response.put("message", folder.getPathFromRoot());
            response.put("folders", folder.getJSONTopLevelFolders());
            System.out.println(response.toString());


            t.sendResponseHeaders(200, response.toString().getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
    }
}
