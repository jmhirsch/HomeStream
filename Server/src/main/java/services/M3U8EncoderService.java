package services;

import controller.Controller;
import model.NetworkFile;
import org.bytedeco.javacpp.Loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class M3U8EncoderService {

    private final String [] LOG_LEVEL = {"-loglevel", "info"};
    private final String SHOULD_OVERRIDE_OUTPUT = "-n"; // -n = no, -y = yes (no prompt)

    private final String pathToMovieCacheFolder;
    private final String pathToPlaylistFile;
    private final String pathToCacheFolder;


    public M3U8EncoderService(String pathToMovieCacheFolder, NetworkFile fileToEncode, String pathToPlaylistFile){
        this.pathToMovieCacheFolder = fileToEncode.getRoot().getFile().getPath() + pathToMovieCacheFolder;
        this.pathToPlaylistFile = pathToPlaylistFile;
        this.pathToCacheFolder = fileToEncode.getRoot().getFile().getPath() + Controller.PATH_TO_CACHE_FOLDER;

        checkAndCreateCacheFolder();

        encodeToM3U8(fileToEncode);
    }

    private void checkAndCreateCacheFolder() {
        if (Files.notExists(Path.of(pathToCacheFolder))){
            try {
                Files.createDirectories(Path.of(pathToCacheFolder));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (Files.notExists(Path.of(this.pathToMovieCacheFolder))){
            try {
                Files.createDirectories(Path.of(this.pathToMovieCacheFolder));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void encodeToM3U8(NetworkFile fileToEncode){
        String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        String extension = fileToEncode.getExtension();


        List<String> commandList = new ArrayList<>();
        commandList.add(ffmpeg); // command
        commandList.add(SHOULD_OVERRIDE_OUTPUT);
        commandList.add("-i"); // specify input file



        if (extension.equals(".mp4")){
            commandList.add(fileToEncode.getFile().getPath());
            commandList.addAll(getMP4EncodeOptions());
        } else if (extension.equals(".mkv")){
            String pathOfNewFileToEncode = pathToMovieCacheFolder + fileToEncode.getNameWithoutExtension() + ".mp4";
            commandList.add(pathOfNewFileToEncode);
            encodeToMP4(fileToEncode);
            commandList.addAll(getMKVEncodeOptions());
        } else if (extension.equals(".avi")){
            String pathOfNewFileToEncode = pathToMovieCacheFolder + fileToEncode.getNameWithoutExtension() + ".mp4";
            commandList.add(pathOfNewFileToEncode);
            encodeToMP4(fileToEncode);
            commandList.addAll(getAVIEncodeOptions());
        }

        commandList.add("-hls_list_size");
        commandList.add("0"); // display all items in M3U8 file
        commandList.addAll(Arrays.asList(LOG_LEVEL)); // set log level
        commandList.add(pathToPlaylistFile); // add output file destination
        ProcessBuilder pb = new ProcessBuilder(commandList);
        startEncoding(pb);
    }



    private void encodeToMP4(NetworkFile fileToEncode) {

        boolean fileIsH264 =  checkCodecIsH264(fileToEncode);

        String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);

        List<String> commandList = new ArrayList<>();
        commandList.add(ffmpeg);
        commandList.add(SHOULD_OVERRIDE_OUTPUT);
        commandList.add("-i");
        commandList.add(fileToEncode.getFile().getPath());
        commandList.addAll(Arrays.asList(LOG_LEVEL));


        if (fileIsH264){
            commandList.add("-codec");
            commandList.add("copy");
        }else{
            commandList.add("-acodec");
            commandList.add("copy");
            commandList.add("-vcodec");
            commandList.add("h264");
        }

        commandList.add(pathToMovieCacheFolder + fileToEncode.getNameWithoutExtension() + ".mp4");

        ProcessBuilder pb = new ProcessBuilder(commandList);
        startEncoding(pb);
    }

    private void startEncoding(ProcessBuilder pb) {
        if (pb != null) {
            try {
                pb.inheritIO().start().waitFor();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<String> getMP4EncodeOptions() {
        List<String> commandList = new ArrayList<>();
        commandList.add("-codec");
        commandList.add("copy"); // copy MP4 Codec to M3U8 files
        return commandList;
    }

    private List<String> getMKVEncodeOptions(){
        List<String> commandList = new ArrayList<>();
        commandList.add("-codec");
        commandList.add("copy"); // copy MP4 Codec to M3U8 files
        return commandList;
    }

    private List<String> getAVIEncodeOptions(){
        List<String> commandList = new ArrayList<>();
        commandList.add("-codec");
        commandList.add("copy"); // copy MP4 Codec to M3U8 files
        return commandList;
    }

    private boolean checkCodecIsH264(NetworkFile fileToEncode) {
        String ffprobe = Loader.load(org.bytedeco.ffmpeg.ffprobe.class);
        ProcessBuilder pb = new ProcessBuilder(ffprobe, "-show_streams", "-i" , fileToEncode.getFile().getPath());

        try {
            final Process process = pb.start();
            process.waitFor();
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            StringBuilder sb = new StringBuilder();

            while ((line = br.readLine())!= null){
                System.out.println(line);
                if (line.contains("codec_long_name")) {
                    sb.append(line + "\n");
                }
            }

            String codecs = sb.toString();
            System.out.println(sb.toString());

            return true;

//            if (codecs.contains("H.264") || codecs.contains("h.264") || codecs.contains("MPEG-4")){
//                return true;
//            }

        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
