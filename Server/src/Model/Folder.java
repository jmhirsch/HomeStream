package Model;

import Enums.FileType;

import java.io.File;
import java.util.ArrayList;

public class Folder extends Filesystem {
    private ArrayList<Folder> folders = new ArrayList<>();
    private ArrayList<CFile> files = new ArrayList<>();

    private String pathFromRoot;

    public Folder(File file, String pathFromRoot) {
        super(file, FileType.FOLDER);
        this.pathFromRoot = "/" + pathFromRoot + file.getName();

        File [] subfolders = file.listFiles(File::isDirectory);
        File [] files = file.listFiles(File::isFile);

        if (subfolders != null) {
            for (File subfolder: subfolders){
                folders.add(new Folder(subfolder, pathFromRoot));
            }
        }

        if (files != null) {
            for (File subfile: files){
                this.files.add(new CFile(subfile));
            }
        }
    }

    public void listAllFolders(){
        super.printName();
        for (Folder folder: folders){
            folder.listAllFolders();
        }
    }

    public void listAllFiles(){
        for (CFile file: files){
            file.printName();
        }
    }

    public String getPathFromRoot(){
        return pathFromRoot;
    }

}
