package Model;

import Enums.FileType;

import java.io.File;

public abstract class Filesystem implements Comparable<Filesystem>{
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

    public String getName(){
        return this.file.getName();
    }

    public int compareTo(Filesystem f2){
        return this.getFile().getName().compareToIgnoreCase(f2.getFile().getName());
    }
}
