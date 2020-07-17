package Controller;

import Model.Folder;
import ServerService.ServerService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpsServer;
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
        Folder root = new Folder(new File(path), "");
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


    public void createContexts(HttpsServer server){
        server.createContext(root.getPathFromRoot(), new MyHandler(root));
    }

     class MyHandler implements HttpHandler {


        private final Folder folder;

        public MyHandler(Folder folder){
            this.folder = folder;
        }

         @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();
            String s = is.readAllBytes().toString();
            System.out.println(t.getRequestURI());
            System.out.println();
            JSONObject msg = new JSONObject();
            msg.put("message", folder.getPathFromRoot());
            String response2 = msg.toString();
            System.out.println(msg);
            t.sendResponseHeaders(200, response2.getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response2.getBytes());
            os.close();
        }
    }
}
