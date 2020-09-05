package services;

import com.flynnbuc.httpserverwrapper.main.ServerController;
import com.flynnbuc.httpserverwrapper.model.Handler;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import controller.Controller;
import model.network.NetworkFile;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

/*
Defines a Service objects which makes a file available for streaming using a M3U8 Playlist (Apple HLS)
Designed to work with Server service. SecureKey is used to authenticate itself with server Requires M3U8 Encoder Service.
 */
public class StreamingService {
    private final String pathToServerDirectory;
    private final String pathToTSCache;
    private final ArrayList<String> paths;
    private final String pathToPlaylist;
    private final String encodedHash;
    PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    public StreamingService(NetworkFile fileToPlay, String url) {
        encodedHash = encodeStringForURLs(String.valueOf(fileToPlay.getHash())); //encode hash of file to play as URL
        pathToTSCache = Controller.PATH_TO_CACHE_FOLDER + "/" + encodedHash + "/"; //Define the path to store the encoded files
        pathToServerDirectory = fileToPlay.getRoot().getFile().getPath(); // get path to root of folder
        pathToPlaylist = pathToServerDirectory + pathToTSCache + encodedHash + ".m3u8"; // define path of the playlist

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {

                return null;
            }
        };
        worker.execute();

        encodeFileIfNotExists(fileToPlay); // encode file if it doesn't already exist

        paths = new ArrayList<>();
        File file = new File(pathToPlaylist);

        reformatM3U8Playlist(file, url); // rewrite M3U8 Playlist to include correct server location
    }

    // Rewrite M3U8 File to contain server address in the name of the TS Files
    private void reformatM3U8Playlist(File file, String url) {
        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);

            String line = "";
            int counter = 0;
            ArrayList<String> linesToWrite = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (line.contains(".ts")) { // checks if current line refers to a Transport Stream file
                    paths.add(pathToTSCache + encodedHash + "" + counter + ".ts");  // save the path to use for contexts
                    counter ++;
                    line = url + paths.get(counter - 1); // defines the new line to write
                }

                // saves the old line back, or the new line if line refered to .TS.
                linesToWrite.add(line);

            }
            br.close();
            reader.close();

            FileWriter writer = new FileWriter(file);
            PrintWriter pw = new PrintWriter(writer);

            // Writes all the lines back. Necessary as file is locked for writing while being read
            for (String nextLine: linesToWrite){
               pw.println(nextLine);
            }

            writer.flush();
            writer.close();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Start encoding service if the file is not already encoded
    private void encodeFileIfNotExists(NetworkFile fileToPlay) {
        if (Files.notExists(Path.of(pathToPlaylist))) {
            new M3U8EncoderService(pathToTSCache, fileToPlay, pathToPlaylist);
        }
    }

    public void addPropertyChangeListnener(PropertyChangeListener listener){
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    //Create contexts for .TS files and M3U8 playlist
    public void createContexts() {
        propertyChangeSupport.firePropertyChange(ServerController.PROPERTY_CHANGE_STR, null, new M3U8PlaylistStreamHandler("/" + encodedHash + "/Play/", pathToPlaylist));
        for (String path : paths) {
            propertyChangeSupport.firePropertyChange(ServerController.PROPERTY_CHANGE_STR, null, new TSStreamHandler(path, new File(pathToServerDirectory + path)));
        }
        System.out.println("Done adding contexts");
    }

    //Returns a URL-conforming String of a given value
    private String encodeStringForURLs(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    // Stream handler for individual Transport Files
    // Will write the file to the client when request is received
    //Header: Content-Type, video/mp2t
    static class TSStreamHandler extends Handler {
        private final File file;

        public TSStreamHandler(String path, File file) {
            super(path);
            this.file = file;
        }

        @Override
        public void handle(HttpExchange exchange) {
            Headers h = exchange.getResponseHeaders();
            h.add("Content-Type", "video/mp2t");

            try {
                FileInputStream is = new FileInputStream(file);
                byte[] bytes = is.readAllBytes();
                int length = bytes.length;

                exchange.sendResponseHeaders(200, length);
                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            } catch (IOException e) {
                System.out.println(e.getMessage() + " in .ts file");
            }
        }
    }

    //Stream handler for M3U8 Playlsit
    // Will write file to client when request is received
    //Header: Content-Type, application/x-mpegURL
    static class M3U8PlaylistStreamHandler extends Handler {
        private final String pathToPlaylist;
        public M3U8PlaylistStreamHandler(String path, String pathToPlaylist) {
            super(path);
            this.pathToPlaylist = pathToPlaylist;
        }

        @Override
        public void handle(HttpExchange t) {
            try {
                File file = new File(pathToPlaylist);
                FileInputStream is = new FileInputStream(file);
                byte[] bytes = is.readAllBytes();
                int length = bytes.length;

                is.close();

                Headers h = t.getResponseHeaders();
                h.add("Content-Type", "application/x-mpegURL");
                t.sendResponseHeaders(200, length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();

                System.out.println("m3u8 file written to client");
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
