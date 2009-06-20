package org.catacomb.interlish.content;

import org.catacomb.be.FileSourcable;
import org.catacomb.interlish.structure.IDd;
import org.catacomb.interlish.structure.Labelled;
import org.catacomb.report.E;

import java.io.File;

public class DocumentedComponent implements IDd, Labelled, FileSourcable {

    public String id;
    public String label;

    public String tag;
    public String info;

    public File sourceFile;

    String shortID;
    BasicTouchTime touchTime;


    boolean justLoaded = true;


    public DocumentedComponent() {
        touchTime = new BasicTouchTime();
        id = "id";
        label = "...";
    }


    public DocumentedComponent(String s) {
        touchTime = new BasicTouchTime();
        id = s;
        label = s;
    }


    public boolean justLoaded() {
        boolean ret = justLoaded;
        justLoaded = false;
        return ret;
    }


    public BasicTouchTime getTouchTime() {
        return touchTime;
    }


    public void setTag(String s) {
        tag = s;
    }


    public void setInfo(String s) {
        info = s;
    }


    public String getTag() {
        return tag;
    }


    public String getInfo() {
        return info;
    }


    public void setID(String s) {
        id = s.trim();
        if (label == null) {
            label = id;
        }
        shortID = null;
    }


    public void setLabel(String s) {
        label = s;
    }


    public boolean hasTag() {
        return (tag != null && tag.length() > 0);
    }


    public boolean hasInfo() {
        return (info != null && info.length() > 0);
    }



    public String getNonTrivialLabel() {
        String ret = label;
        if (ret == null || ret.length() == 0 || ret.equals("...")) {
            ret = id;
        }
        return ret;
    }


    public String getLabel() {
        return label;
    }


    public String getID() {
        return id.trim();
    }

    public String getFullID() {
        String ret = getID();
        if (ret.indexOf(".") < 0) {
            E.warning("probably need to qualify " + ret);
        }
        return ret;
    }


    public String getShortID() {
        if (shortID == null) {
            shortID = id.substring(id.lastIndexOf(".") + 1, id.length());
        }
        return shortID;
    }


    public String getTypeID() {
        String s = getClass().getName();
        s = s.substring(s.lastIndexOf(".") + 1, s.length());
        return s;
    }


    public File getSourceFile() {
        return sourceFile;
    }


    public void setSourceFile(File f) {
        sourceFile = f;
    }

}
