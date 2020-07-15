package Model;


import Enums.FileType;

import java.io.File;

public class CFile extends Filesystem {

    public CFile(File file) {
        super(file, FileType.FILE);
    }
}
