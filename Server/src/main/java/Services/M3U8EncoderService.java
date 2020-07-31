package Services;

import Controller.Controller;
import Model.CFile;
import org.bytedeco.javacpp.Loader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class M3U8EncoderService {

    private final String pathToMovieCacheFolder;
    private final CFile fileToEncode;
    private final String pathToPlaylistFile;
    private final String pathToCacheFolder;


    public M3U8EncoderService(String pathToMovieCacheFolder, CFile fileToEncode, String pathToPlaylistFile){
        this.pathToMovieCacheFolder = fileToEncode.getRoot().getFile().getPath() + pathToMovieCacheFolder;
        this.fileToEncode = fileToEncode;
        this.pathToPlaylistFile = pathToPlaylistFile;
        this.pathToCacheFolder = fileToEncode.getRoot().getFile().getPath() + Controller.PATH_TO_CACHE_FOLDER;

        checkAndCreateCacheFolder();

        encodeMP4ToM3U8(fileToEncode);
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

    private void encodeMP4ToM3U8(CFile fileToEncode) {
        String ffmpeg = Loader.load(org.bytedeco.ffmpeg.ffmpeg.class);
        ProcessBuilder pb = new ProcessBuilder(ffmpeg, "-i", fileToEncode.getFile().getPath(), "-codec", "copy", "-hls_list_size", "0", this.pathToPlaylistFile);
        try {
            pb.inheritIO().start().waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
