package Services;

import Model.CFile;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;

public class StreamingService extends ServerService {

    private CFile fileToPlay;


    public StreamingService(int socketNum, CFile fileToPlay) {
        super(socketNum);
        this.fileToPlay = fileToPlay;
        System.out.println("streaming service for file " + fileToPlay.getName() + " has started");
    }

    @Override
    public void startServer(Function<HttpServer, Void> contextCreator){
        super.startServer(contextCreator);
        getServer().createContext("/play", new StreamHandler());
    }

    static class StreamHandler implements HttpHandler {

        private final CFile fileToPlay;
        private final File file;

        public StreamHandler(CFile fileToPlay){
            this.fileToPlay = fileToPlay;
            this.file = fileToPlay.getFile();

        }

        @Override
        public void handle(HttpExchange t) throws IOException {

            JSONObject response = new JSONObject();
            response.put("message", "a message");
            System.out.println(response.toString());

            FileInputStream is = new FileInputStream(file);


            t.sendResponseHeaders(200, response.toString().getBytes().length);
            OutputStream os = t.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
    }
}
