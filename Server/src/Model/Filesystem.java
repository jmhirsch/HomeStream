package Model;

import Enums.FileType;

import java.io.File;
import java.nio.file.*;

public abstract class Filesystem {
    private final File file;
    private final FileType type;

    public Filesystem(File file, FileType type){
       this.file = file;
        this.type = type;
    }

    public void printName(){
        System.out.println(file);
    }

    public void printPath(){
        printPath();
    }

    public File getFile() {
        return file;
    }

    public FileType type(){
        return type;
    }
}
