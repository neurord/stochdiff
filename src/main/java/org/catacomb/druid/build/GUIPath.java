package org.catacomb.druid.build;


public class GUIPath {


    public String path;

    boolean unique;


    public GUIPath() {
        this("");
    }


    public GUIPath(String psf) {
        path = psf;
        unique = false;
    }


    public GUIPath(String psf, String id) {
        if (psf != null && psf.length() > 0) {
            path = psf + "." + id;
        } else {
            path = id;
        }
        unique = true;
    }


    public String toString() {
        return path;
    }


    public boolean isUnique() {
        return unique;
    }


    public String getPath() {
        return path;
    }


    public GUIPath extend(String id) {
        GUIPath ret = null;

        if (id != null && id.length() > 0) {
            ret = new GUIPath(path, id);
        } else {
            ret = new GUIPath(path);
            // could return this instead of a new one if could handle unique flag EFF
        }
        return ret;
    }

}
