package org.catacomb.util;


import java.util.ArrayList;
import java.util.HashMap;


public class DirRef {

    String dirName;

    HashMap<String, DirRef> dirs;


    ArrayList<FileRef> files;


    public DirRef(String s) {
        dirName = s;
        dirs = new HashMap<String, DirRef>();
        files = new ArrayList<FileRef>();
    }

    public void add(DirRef dr) {
        dirs.put(dr.getDirName(), dr);
    }

    private String getDirName() {
        // TODO Auto-generated method stub
        return null;
    }

    public void add(FileRef fr) {
        files.add(fr);
    }

    public boolean containsDir(String s) {
        return (dirs.containsKey(s));
    }

    public DirRef getDir(String s) {
        return dirs.get(s);
    }



}
