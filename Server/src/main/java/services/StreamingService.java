package services;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import controller.Controller;
import model.CFile;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;

public class StreamingService implements AutoCloseable {

    private CFile fileToPlay;
    private double secureKey;
    private String pathToServerDirectory;
    private String pathToTSCache;
    private ArrayList<String> paths;
    private String pathToPlaylist;
    private final String encodedHash;


    public StreamingService(double secureKey, CFile fileToPlay) {
        this.secureKey = secureKey;
        this.fileToPlay = fileToPlay;
        encodedHash = encodeValue("" + fileToPlay.getHash());
        pathToTSCache = Controller.PATH_TO_CACHE_FOLDER + "/" + encodedHash + "/";
        pathToServerDirectory = fileToPlay.getRoot().getFile().getPath();
        pathToPlaylist = pathToServerDirectory + pathToTSCache + encodedHash + ".m3u8";

        if (Files.notExists(Path.of(pathToPlaylist))) {

            M3U8EncoderService encoderService = new M3U8EncoderService(pathToTSCache, fileToPlay, pathToPlaylist);
        }

        paths = new ArrayList<>();
        File file = new File(pathToPlaylist);

        try {
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);

            String line = "";
            int counter = 0;
            ArrayList<String> linesToWrite = new ArrayList<>();
            while ((line = br.readLine()) != null) {
                if (line.contains(".ts")) {
                    paths.add(pathToTSCache + encodedHash + "" + counter + ".ts");
                    counter ++;
                    line = "http://nissa.local:3004" + paths.get(counter - 1);
                }

                linesToWrite.add(line);

            }
            br.close();
            reader.close();

            FileWriter writer = new FileWriter(file);
            PrintWriter pw = new PrintWriter(writer);

            for (String nextLine: linesToWrite){
               pw.println(nextLine);
            }

            writer.flush();
            writer.close();
            pw.close();
            boolean added = ServerService.getInstance().addContext(secureKey, "/" + encodedHash + "/Play/", new M3U8PlaylistStreamHandler(pathToPlaylist));
            //System.out.println(fileToPlay.getPathFromRoot() + "/Play/");

            for (String path : paths) {
                boolean add = ServerService.getInstance().addContext(secureKey, path, new TSStreamHandler(new File(pathToServerDirectory + path)));
                System.out.println(pathToServerDirectory + path);
                //System.out.println("Adding " + path + " added: " + add);
            }

            //System.out.println("Streaming path added???? " + added);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }


    @Override
    public void close() {
        ServerService.getInstance().removeContext(secureKey, pathToTSCache);
        secureKey = new Random().nextDouble();
        System.out.println("Close has been called1");
    }


    static class TSStreamHandler implements HttpHandler {
        private final File file;

        public TSStreamHandler(File file) {
            this.file = file;
            //System.out.println(file.toString());
        }

        @Override
        public void handle(HttpExchange exchange) {

            //System.out.println("Reading .ts files");
            Headers h = exchange.getResponseHeaders();

            h.add("Content-Type", "video/mp2t");

            try {
                FileInputStream is = new FileInputStream(file);
                byte[] bytes = is.readAllBytes();
                int length = bytes.length;

                exchange.sendResponseHeaders(200, length);
                OutputStream os = exchange.getResponseBody();
                //System.out.println("Writing...");
                os.write(bytes);

                os.close();


            } catch (IOException e) {
                System.out.println(e.getMessage() + " in .ts file");
            }
        }
    }

    static class M3U8PlaylistStreamHandler implements HttpHandler {

        private final String pathToPlaylist;

        public M3U8PlaylistStreamHandler(String pathToPlaylist) {
            this.pathToPlaylist = pathToPlaylist;
        }

        @Override
        public void handle(HttpExchange t) {
            try {
                System.out.println("Reading m3u8 File ");
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
