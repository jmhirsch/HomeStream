package services;

import exceptions.CouldNotFindIPException;
import controller.Main;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;


public class ServerService {

    private static ServerService instance = null;
    private InetSocketAddress address;
    private HttpServer server;

    private double secureKey;

    private ServerService(){}

    public static ServerService getInstance(){
        if (instance == null){
            instance = new ServerService();
        }
        return instance;
    }



    public double startServer(Function <Double, Void> contextCreator, int portNum){
        secureKey = new Random().nextDouble();
        address = new InetSocketAddress(portNum);
        try {
            server = HttpServer.create(address, 0);
            server.setExecutor(null);
            server.start();
            contextCreator.apply(secureKey);
        } catch (IOException e) {
            e.printStackTrace();
        } {
        }

        return secureKey;
    }

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

    public boolean addContext(double secureKey, String path, HttpHandler handler){
        if (authenticate(secureKey)){
            server.createContext(path, handler);
            return true;
        }
        return false;
    }


    public boolean removeContext(double secureKey, String path){
        if (authenticate(secureKey)){
            server.removeContext(path);
            return true;
        }
        return false;
    }

    private boolean authenticate(double secureKey){
        return Objects.hash(address, server, secureKey) == this.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerService that = (ServerService) o;
        return Double.compare(that.secureKey, secureKey) == 0 &&
                address.equals(that.address) &&
                server.equals(that.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, server, secureKey);
    }

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
