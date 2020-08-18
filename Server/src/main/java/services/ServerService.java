package services;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import controller.Main;
import exceptions.CouldNotFindIPException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Objects;
import java.util.function.Function;

/*
Defines an HTTP Server singleton with portnum
When starting the server,
 */
public class ServerService {

    private InetSocketAddress address;
    private HttpServer server;
    private int portNum;

    public ServerService(int portNum){
        this.portNum = portNum;
    }

    public ServerService(){}


    public int getPortNum() {
        return portNum;
    }

    public boolean startServer(int portNum, Function<Void, Void> contextCreator){
        this.portNum = portNum;
        return startServer(contextCreator);
    }

    //Stars server using specified port num, returns true if server is created, false otherwise
    //Caller receives a callback function to create contexts, with the secureKey as a parameter.
    // It is expected that the caller will save the secureKey for future use. Contexts can be created
    // later using the secureKey
    public boolean startServer(Function <Void, Void> contextCreator){
        if (portNum == -1){
            return false;
        }
        try {
            address = new InetSocketAddress(portNum);
            server = HttpServer.create(address, 0);
            server.setExecutor(null);
            server.start();
            contextCreator.apply(null);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } {
        }
        return true;
    }

    //Stops the server. Requires secureKey. Returns true IFF secureKey matches
    public void exit() {
            try {
                server.stop(0);
                System.out.println("Server Closed");
            } catch(Exception e) {
                System.err.println("Server exited with error : " + e.getLocalizedMessage());
            }
    }

    //Adds specified context. Requires secure key. Returns true IFF secureKey matches
    public void addContext(String path, HttpHandler handler){
            server.createContext(path, handler);
    }


    //Removes context at specified path. Returns true IFF secureKey matches
    public void removeContext(String path){
            try {
                server.removeContext(path);
            } catch(Exception e){
                //System.err.println("Error in server for path: " + path + " " +e.getMessage());
            }
    }

    //Private method used to authenticate caller using secureKey. Returns true IFF secureKey matches,
    // causing hashcodes to be equal
    private boolean authenticate(double secureKey){
        return Objects.hash(address, server, secureKey) == this.hashCode();
    }

    //Returns localIP of server
    public static String getLocalIP() throws CouldNotFindIPException {
        String ip = null;

        if (Main.systemIsMacOS()) {
            try {
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress("google.com", 80));
                ip = socket.getLocalAddress().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                ip = socket.getLocalAddress().getHostAddress();
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
        }

        if (ip == null){
            throw new CouldNotFindIPException();
        }

        return ip.replace("/", "");
    }

    //Returns remoteIP of server
    public static String getRemoteIP() throws CouldNotFindIPException{
        String ip = "";
        URL whatismyip = null;
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));

            ip = in.readLine(); //you get the IP as a String
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (ip == null){
            throw new CouldNotFindIPException();
        }

        return ip;
    }
}
