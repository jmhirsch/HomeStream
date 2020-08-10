package services;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.Controller;
import model.NetworkFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

/*
Defines a Service objects which makes a file available for streaming using a M3U8 Playlist (Apple HLS)
Designed to work with Server service. SecureKey is used to authenticate itself with server Requires M3U8 Encoder Service.
 */
public class StreamingService implements AutoCloseable {

    private double secureKey;
    private final String pathToServerDirectory;
    private final String pathToTSCache;
    private final ArrayList<String> paths;
    private final String pathToPlaylist;
    private final String encodedHash;


    public StreamingService(double secureKey, NetworkFile fileToPlay) {
        this.secureKey = secureKey; // key used to identify to server
        encodedHash = encodeStringForURLs(String.valueOf(fileToPlay.getHash())); //encode hash of file to play as URL
        pathToTSCache = Controller.PATH_TO_CACHE_FOLDER + "/" + encodedHash + "/"; //Define the path to store the encoded files
        pathToServerDirectory = fileToPlay.getRoot().getFile().getPath(); // get path to root of folder
        pathToPlaylist = pathToServerDirectory + pathToTSCache + encodedHash + ".m3u8"; // define path of the playlist

        encodeFileIfNotExists(fileToPlay); // encode file if it doesn't already exist

        paths = new ArrayList<>();
        File file = new File(pathToPlaylist);

        reformatM3U8Playlist(file); // rewrite M3U8 Playlist to include correct server location
        createContexts(secureKey); // create appropriate contexts in server
    }

    // Rewrite M3U8 File to contain server address in the name of the TS Files
    private void reformatM3U8Playlist(File file) {
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
                    line = "http://nissa.local:3004" + paths.get(counter - 1); // defines the new line to write
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

    //Create contexts for .TS files and M3U8 playlist
    private void createContexts(double secureKey) {
        ServerService.getInstance().addContext(secureKey, "/" + encodedHash + "/Play/", new M3U8PlaylistStreamHandler(pathToPlaylist));
        for (String path : paths) {
            ServerService.getInstance().addContext(secureKey, path, new TSStreamHandler(new File(pathToServerDirectory + path)));
        }
    }

    //Returns a URL-conforming String of a given value
    private String encodeStringForURLs(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }


    //Method to call when object is closed (currently does not work afaik)
    @Override
    public void close() {
        ServerService.getInstance().removeContext(secureKey, pathToTSCache);
        secureKey = new Random().nextDouble();
        System.out.println("Close has been called1");
    }


    // Stream handler for individual Transport Files
    // Will write the file to the client when request is received
    //Header: Content-Type, video/mp2t
    static class TSStreamHandler implements HttpHandler {
        private final File file;

        public TSStreamHandler(File file) {
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
    static class M3U8PlaylistStreamHandler implements HttpHandler {
        private final String pathToPlaylist;
        public M3U8PlaylistStreamHandler(String pathToPlaylist) {
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
