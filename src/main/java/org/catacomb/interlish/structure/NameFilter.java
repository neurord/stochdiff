package org.catacomb.interlish.structure;


public interface NameFilter {

    public final static int YES = 1;
    public final static int NO = -1;
    public final static int DONTKNOW = 0;


    int fileStatus(String s);

    int folderStatus(String s);


    boolean acceptFile(String s);

    boolean acceptFolder(String name);

}
