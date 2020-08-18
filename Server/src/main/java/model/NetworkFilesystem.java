package model;

import enums.FileType;

import java.io.File;

/*
Abstract filesystem objects which represents files and folders that will be sent through the server
 */
public abstract class NetworkFilesystem implements Comparable<NetworkFilesystem>{
    private final File file;
    private final FileType type;
    private final long hash; // unique hash generated for each file and each folder

    private final NetworkFilesystem root; // root object
    protected String pathFromRoot; // path from the root object

// Constructor for root object
    public NetworkFilesystem(File file, FileType type, long hash){
        this.file = file;
        this.type = type;
        this.root = this;
        this.hash = hash(hash);
    }

    public NetworkFilesystem(File file, FileType type, NetworkFilesystem root, long hash){
        this.file = file;
        this.type = type;
        this.root = root;
        this.hash = hash(hash);
    }

    public long getHash(){
        return hash;
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

    public NetworkFilesystem getRoot(){
        return root;
    }

    public int compareTo(NetworkFilesystem f2){
        return this.getFile().getName().compareToIgnoreCase(f2.getFile().getName());
    }

    //Generate a hashcode using another hashcode as a base
    protected long hash(long previousHash){
        return this.getName().hashCode() + this.type.hashCode() + previousHash;
        
    }
}
