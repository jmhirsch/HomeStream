package ServerService;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.function.Function;


public class ServerService {

    private InetSocketAddress address;
    private HttpServer server;

    public ServerService(int socketNum){
        address = new InetSocketAddress(socketNum);

    }

    public void startServer(Function <HttpServer, Void> contextCreator){
        try {
            server = HttpServer.create(address, 0);
            server.setExecutor(null);
            server.start();
            contextCreator.apply(server);
        } catch (IOException e) {
            e.printStackTrace();
        } {
        }
    }

    public void exit() {
        try {
            server.stop(0);
        } finally {
            System.out.println("Server Closed");
        }
    }
}
