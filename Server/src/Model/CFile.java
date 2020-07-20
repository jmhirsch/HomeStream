package Model;


import Enums.FileType;

import java.io.File;

public class CFile extends Filesystem {

    public CFile(File file, String pathFromRoot) {
        super(file, FileType.FILE);

        this.pathFromRoot = pathFromRoot + "/" + getName();
    }
}
