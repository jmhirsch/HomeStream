package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import enums.Notification;
import interfaces.NotificationListener;
import model.Context;
import model.requests.*;
import observer.Observer;
import org.json.JSONObject;
import org.json.JSONTokener;
import services.ServerService;

import java.io.*;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ServerController implements Observer {

    private ServerService serverService;
    private final ControllerManager manager;
    private NotificationListener notificationListener;

    private final RequestNumberGenerator numGenerator = new RequestNumberGenerator();

    Map<Long, NetworkRequest> requestQueue = new ConcurrentHashMap<>();

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

    public synchronized void createContexts(List<Context> contextsToCreate) {
        for (Context context : contextsToCreate) {
            String path = context.getPath();
            switch (context.getType()) {
                case REFRESH_CONTEXT -> serverService.addContext(path, new RefreshHandler(path));
                case GET_DATA_CONTEXT -> serverService.addContext(path, new GetDataHandler(path));
                case PATCH_FILE_FOLDER -> serverService.addContext(path, new PatchFileFolderHandler(path));
                case STREAMING_START_CONTEXT -> serverService.addContext(path, new FileStreamingRequestHandler(path));
                default -> System.err.println("Unknown context type or type not defined for context at path " + context.getPath() + " contextType: " + context.getType());
            }
        }
    }

    public void removeContext(String path) {
        serverService.removeContext(path);
    }

    public synchronized void handleRequestResponse(long requestNum, JSONObject response, boolean success) {
        int responseCode = success ? 200 : 400;
        NetworkRequest request = requestQueue.remove(requestNum);

        if (request == null) {
            System.out.println("request is null");
        }

        respondToRequest(response, request.getExchange(), responseCode);
    }

    public synchronized void respondToRequest(JSONObject response, HttpExchange exchange, int responseCode) {

        System.out.println(response.toString());
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

    private class PatchFileFolderHandler extends Handler {

        public PatchFileFolderHandler(String path) {
            super(path);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            String requestStr = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            JSONTokener parser = new JSONTokener(requestStr);
            JSONObject requestObject = new JSONObject(parser);

            if (exchange.getRequestMethod().equalsIgnoreCase("PATCH") && exchange.getRequestURI().toString().equals(getPath())) {
                FileUpdateRequest request = new FileUpdateRequest(numGenerator.incrementAndGet(), exchange, getPath());
                requestQueue.put(request.getRequestNum(), request);
                notificationListener.notificationReceived(Notification.PATCH_WITH_HASH, requestObject, request.getRequestNum());
            } else {
                JSONObject response = new JSONObject();
                response.put("message", "invalid request");
                respondToRequest(response, exchange, 406); // Not Acceptable
            }

        }
    }

    private class FileStreamingRequestHandler extends Handler {
        public FileStreamingRequestHandler(String path) {
            super(path);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            InputStream is = exchange.getRequestBody();
            String requestStr = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));
            JSONTokener parser = new JSONTokener(requestStr);
            JSONObject object = new JSONObject(parser);

            if (exchange.getRequestURI().toString().equalsIgnoreCase(getPath())) {
                StreamingRequest request = new StreamingRequest(numGenerator.incrementAndGet(), exchange, getPath());
                requestQueue.put(request.getRequestNum(), request);
                notificationListener.notificationReceived(Notification.FILE_STREAMING_REQUESTED, object, request.getRequestNum());
            } else {
                JSONObject response = new JSONObject();
                response.put("message", "invalid request");
                respondToRequest(response, exchange, 406); // Not Acceptable
            }
        }
    }

    private class GetDataHandler extends Handler {
        public GetDataHandler(String path) {
            super(path);
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Get_Data received");
            URI uri = exchange.getRequestURI();
            if (uri.getPath().equals(getPath()) || uri.getPath().equals(getPath() + "/")) {
                GetRequest request = new GetRequest(numGenerator.incrementAndGet(), exchange, getPath());
                requestQueue.put(request.getRequestNum(), request);
                notificationListener.notificationReceived(Notification.GET_CALLED, request, request.getRequestNum());
            } else {
                JSONObject response = new JSONObject();
                response.put("message", "path " + uri.getPath() + " not found");
                respondToRequest(response, exchange, 404);
            }
        }
    }

    private class RefreshHandler implements HttpHandler {
        private final String path;

        public RefreshHandler(String path) {
            this.path = path;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("refresh received");
            RefreshRequest refreshRequest = new RefreshRequest(numGenerator.incrementAndGet(), exchange, path);
            requestQueue.put(refreshRequest.getRequestNum(), refreshRequest);
            notificationListener.notificationReceived(Notification.REFRESH_CALLED, refreshRequest, refreshRequest.getRequestNum());
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


