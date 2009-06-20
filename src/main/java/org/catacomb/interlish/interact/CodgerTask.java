package org.catacomb.interlish.interact;

import java.io.File;


public class CodgerTask {

    String[] exports;
    File rootDir;


    public CodgerTask(File f, String[] sa) {
        rootDir = f;
        exports = sa;
    }

    public File getRootDir() {
        return rootDir;
    }

    public String[] getExports() {
        return exports;
    }

}
