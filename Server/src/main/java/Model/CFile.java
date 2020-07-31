package Model;


import Enums.FileType;
import java.io.File;

public class CFile extends Filesystem {


    private final String nameStripExtension;
    private final String extension;

    public CFile(File file, String pathFromRoot, Filesystem root) {
        super(file, FileType.FILE, root);

        this.pathFromRoot = pathFromRoot + "/" + getName();

        int extensionIndex = file.getName().lastIndexOf('.');
        this.nameStripExtension = file.getName().substring(0, extensionIndex);
        this.extension = file.getName().substring(extensionIndex);
    }


    public String getNameStripExtension() {
        return this.nameStripExtension;
    }

    public String getExtension(){
        return this.extension;
    }
}
