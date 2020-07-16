package ServerService;

import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Function;

public class ServerService {

    private ServerSocket server;
    private Socket socket;
    private DataInputStream in;

    private int socketNum;
    private Thread serverThread;



    public ServerService(int socketNum){
        this.socketNum = socketNum;

    }

    public void startServer(Function<String, Void> callback){

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    server = new ServerSocket(socketNum);

                    System.out.println("Server Started");

                    socket = server.accept();
                    in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

                    String line = "";

                    while(!line.equals("exit")){
                        line = in.readUTF();
                        callback.apply(line);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    try {
                        server.close();
                        socket.close();
                        in.close();
                        System.out.println("Exited Server");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        serverThread = new Thread(runnable);
        serverThread.start();
    }

    public void exit() {
        serverThread.interrupt();
        try {
            server.close();
        } catch (IOException e) {

        }
        finally {
            System.out.println("Server Closed");
        }
    }
}
