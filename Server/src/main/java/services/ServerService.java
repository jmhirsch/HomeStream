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
import java.util.Random;
import java.util.function.Function;

/*
Defines an HTTP Server singleton with portnum
When starting the server, a unique secureKey is created. This is used for authentication purposes
Only classes with the secureKey can add or remove contexts to the server. This prevent unauthorized modification of the server.
All classes which wish to modify the server must be passed the secureKey
SecureKey is then used to create a hash and compare the private hashcode with the hashcode generated using the secure key during
authentication
 */
public class ServerService {
    private static ServerService instance = null;

    private InetSocketAddress address;
    private HttpServer server;
    private Double secureKey;
    private ServerService(){}

    public static ServerService getInstance(){
        if (instance == null){
            instance = new ServerService();
        }
        return instance;
    }

    //Stars server using specified port num, returns true if server is created, false otherwise
    //Caller receives a callback function to create contexts, with the secureKey as a parameter.
    // It is expected that the caller will save the secureKey for future use. Contexts can be created
    // later using the secureKey
    public boolean startServer(Function <Double, Void> contextCreator, int portNum){

        address = new InetSocketAddress(portNum);
        try {
            server = HttpServer.create(address, 0);
            server.setExecutor(null);
            server.start();
            secureKey = new Random().nextDouble() * server.hashCode() * address.hashCode();
            contextCreator.apply(secureKey);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } {
        }
        return true;
    }

    //Stops the server. Requires secureKey. Returns true IFF secureKey matches
    public boolean exit(double secureKey) {
        if (authenticate(secureKey)) {
            try {
                server.stop(0);
            } finally {
                System.out.println("Server Closed");
            }
            return true;
        }
        return false;
    }

    //Adds specified context. Requires secure key. Returns true IFF secureKey matches
    public boolean addContext(double secureKey, String path, HttpHandler handler){
        if (authenticate(secureKey)){
            server.createContext(path, handler);
            return true;
        }
        return false;
    }


    //Removes context at specified path. Returns true IFF secureKey mathces
    public boolean removeContext(double secureKey, String path){
        if (authenticate(secureKey)){
            server.removeContext(path);
            return true;
        }
        return false;
    }

    //Private method used to authenticate caller using secureKey. Returns true IFF secureKey matches,
    // causing hashcodes to be equal
    private boolean authenticate(double secureKey){
        return Objects.hash(address, server, secureKey) == this.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerService that = (ServerService) o;
        return  Double.compare(that.secureKey, secureKey) == 0 &&
                address.equals(that.address) &&
                server.equals(that.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, server);
    }

    //Returns localIP of server
    public String getLocalIP() throws CouldNotFindIPException {
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
    public String getRemoteIP() throws CouldNotFindIPException{
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
