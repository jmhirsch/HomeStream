package Model;

import java.nio.file.FileSystem;

public abstract class Filesystem {
    public String name;
    public String pathFromRoot;

    public Filesystem(String name, String pathFromRoot){
        this.name = name;
        this.pathFromRoot = pathFromRoot;
    }

    public void printName(){
        System.out.println(name);
    }

    public void printPath(){
        printPath();
    }

    public String getName() {
        return name;
    }

    public String getPathFromRoot() {
        return pathFromRoot;
    }
}
