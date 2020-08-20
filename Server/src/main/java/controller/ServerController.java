package controller;

import com.sun.net.httpserver.HttpExchange;
import enums.ServerMethodType;
import interfaces.NotificationListener;
import model.requests.Context;
import model.requests.Handler;
import model.requests.NetworkRequest;
import model.requests.Notification;
import observer.Observer;
import org.json.JSONObject;
import org.json.JSONTokener;
import services.ServerService;

import java.io.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServerController implements Observer {

    private ServerService serverService;
    private final ControllerManager manager;
    private NotificationListener notificationListener;

    private final RequestNumberGenerator numGenerator = new RequestNumberGenerator();

    Map<Long, NetworkRequest<JSONObject>> requestQueue = new ConcurrentHashMap<>();

    public ServerController(ControllerManager manager) {
        serverService = new ServerService();
        this.manager = manager;
    }

    public void setNotificationListener(NotificationListener notificationListener) {
        this.notificationListener = notificationListener;
    }

    public synchronized void startServerService(int portNum, Function<Boolean, Void> callback) {
        boolean created = serverService.startServer(portNum, this::createContexts);
        callback.apply(created);
        manager.requestData();
    }

    private synchronized Void createContexts(Void secureKey) {
        return null;
    }

    public synchronized void stopServerService(Function<Boolean, Void> callback) {
        serverService.exit();
        serverService = null;
        callback.apply(false);
    }

    public synchronized void createContexts(Context ... contextsToCreate) {
            for (Context context : contextsToCreate) {
            serverService.addContext(context.path(), new ServerHandler(context.path(), context.type(), context.notification()));
        }
    }

    public void removeContext(String path) {
        serverService.removeContext(path);
    }

    public synchronized void handleRequestResponse(long requestNum, JSONObject response, int responseCode) {
        NetworkRequest<JSONObject> request = requestQueue.remove(requestNum);
        if (request == null) {
            System.out.println("request is null");
        }else {
            respondToRequest(response, request.getExchange(), responseCode);
        }
    }

    public synchronized void respondToRequest(JSONObject response, HttpExchange exchange, int responseCode) {
        try {
            byte[] responseBytes = response.toString().getBytes();
            exchange.sendResponseHeaders(responseCode, responseBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(responseBytes);
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update() {
        System.out.println("No action defined in update() for ServerController. Use update(Object obj) with an HTTPHandler");
    }

    @Override
    public void update(Object obj) {
        if (obj instanceof Handler httpHandler){
            serverService.addContext(httpHandler.getPath(), httpHandler);
        }
    }

    private class ServerHandler extends Handler{
        private final ServerMethodType type;
        private final Notification notification;
        public ServerHandler(String path, ServerMethodType type, Notification notification){
            super(path);
            this.type = type;
            this.notification = notification;

        }

        @Override
        public void handle(HttpExchange exchange) {
            try {
                JSONObject response = new JSONObject();
                if (!exchange.getRequestURI().toString().equalsIgnoreCase(getPath())) {
                    response.put("message", "invalid path " + getPath());
                    respondToRequest(response, exchange, 404); // Invalid URL
                    return;
                } else if (!exchange.getRequestMethod().equalsIgnoreCase(type.toString())) {
                    response.put("message", "invalid request type " + exchange.getRequestMethod() + " at path " + getPath());
                    respondToRequest(response, exchange, 406); // Not Acceptable
                    return;
                }
                JSONObject requestBody;
                InputStream is = exchange.getRequestBody();
                String requestStr = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
                if (requestStr.trim().equalsIgnoreCase("") || requestStr.isBlank() || requestStr.isEmpty()) {
                    requestBody = new JSONObject();

                }else{
                    JSONTokener parser = new JSONTokener(requestStr);
                    requestBody = new JSONObject(parser);
                }

                if (exchange.getRequestURI().toString().equalsIgnoreCase(getPath())) {
                    NetworkRequest<JSONObject> request = new NetworkRequest<>(numGenerator.incrementAndGet(), exchange, getPath());
                    requestQueue.put(request.getRequestNum(), request);
                    notificationListener.notificationReceived(notification, requestBody, request.getRequestNum());
                }
            }catch(Exception e){
                System.err.println(e.getLocalizedMessage());
            }
        }
    }

    static class RequestNumberGenerator {
        private long requestNum;

        public synchronized long incrementAndGet() {
            if (requestNum >= Long.MAX_VALUE) {
                requestNum = Long.MIN_VALUE;
            }
            return ++requestNum;
        }
    }
}


