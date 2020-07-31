package Model;

import Enums.FileType;

import java.io.File;

public abstract class Filesystem implements Comparable<Filesystem>{
    private final File file;
    private final FileType type;

    private final Filesystem root;
    protected String pathFromRoot;


    public Filesystem(File file, FileType type){
        this.file = file;
        this.type = type;
        this.root = this;
    }

    public Filesystem(File file, FileType type, Filesystem root){
        this.file = file;
        this.type = type;
        this.root = root;
    }

    public void printName(){
        System.out.println(file);
    }

    public void printPath(){
        System.out.println(file.getPath());
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

    public String getPathFromRoot(){
        return pathFromRoot;
    }

    public Filesystem getRoot(){
        return root;
    }

    public int compareTo(Filesystem f2){
        return this.getFile().getName().compareToIgnoreCase(f2.getFile().getName());
    }
}
