package org.catacomb.druid.load;


import org.catacomb.serial.quickxml.XMLFileElement;

import java.io.File;




public class Settings {


    XMLFileElement xrecent;
    XMLFileElement xpref;


    public Settings(String appname) {

        File fuser = new File(System.getProperty("user.home"));
        File fccmb = new File(fuser, ".catacomb");
        if (fccmb.exists()) {
            // OK;
        } else {
            fccmb.mkdir();
        }

        File fapp = new File(fccmb, appname);
        if (fapp.exists()) {
            // OK;
        } else {
            fapp.mkdir();
        }


        xrecent = new XMLFileElement(fapp, "recent");
        xpref = new XMLFileElement(fapp, "preferences");
    }


    public void addRecentFile(File f) {
        addRecentPath(f.getAbsolutePath());
    }


    public void addRecentPath(String fpath) {
        xrecent.prependElementUnique("path", fpath);
        xrecent.limitNumber("path", 10);
        xrecent.sync();
    }


    public String[] getRecentPaths() {
        return xrecent.getValues("path");
    }



    public void addElement(String name, String value) {
        xpref.prependElementUnique(name, value);
        xpref.sync();
    }

    public boolean hasElement(String name, String value) {
        return xpref.hasElement(name, value);
    }

    public boolean hasElement(String name) {
        return xpref.hasElement(name);
    }

    public String getValue(String eltname) {
        return xpref.getValue(eltname);
    }

    public void setElement(String name, String value) {
        xpref.setElement(name, value);
        xpref.sync();
    }

}
