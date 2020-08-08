package model;

import enums.FileType;

import java.io.File;

public abstract class Filesystem implements Comparable<Filesystem>{
    private final File file;
    private final FileType type;
    private final double hash;

    private final Filesystem root;
    protected String pathFromRoot;


    public Filesystem(File file, FileType type, double hash){
        this.file = file;
        this.type = type;
        this.root = this;
        this.hash = hash(hash);
    }

    public Filesystem(File file, FileType type, Filesystem root, double hash){
        this.file = file;
        this.type = type;
        this.root = root;
        this.hash = hash(hash);
    }

    public double getHash(){
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

    public Filesystem getRoot(){
        return root;
    }

    public int compareTo(Filesystem f2){
        return this.getFile().getName().compareToIgnoreCase(f2.getFile().getName());
    }

    protected double hash(double previousHash){
        return this.getName().hashCode() + this.type.hashCode() + previousHash;
    }

}
