package org.catacomb.druid.swing;

import java.io.File;
import java.util.HashMap;

import javax.swing.JFileChooser;


public final class DFileChooser extends JFileChooser {
    static final long serialVersionUID = 1001;

    ExtFileFilter defaultFilter;

    HashMap<String, ExtFileFilter> filters = new HashMap<String, ExtFileFilter>();


    public DFileChooser() {

        setFileSelectionMode(FILES_ONLY);

    }


    public void addExtensionFilter(String ext, String desc) {
        if (filters.containsKey(ext)) {
            //
        } else {
            ExtFileFilter eff = new ExtFileFilter(ext, desc);
            filters.put(ext, eff);
            addChoosableFileFilter(eff);
            if (defaultFilter == null) {
                defaultFilter = eff;
            }
        }
        setFileFilter(filters.get(ext));
    }


    public void setFilter(String ext) {
        setFileFilter(filters.get(ext));
    }


    //setApproveButtonText("Run Application");

    public void setSelectFiles() {
        setFileSelectionMode(FILES_ONLY);
    }

    public void setSelectDirectories() {
        setFileSelectionMode(DIRECTORIES_ONLY);
    }




    /*
    public void setOwnFilter(String s) {
       for (int i = 0; i < vf.size(); i++) {
     ExtFileFilter eff = (ExtFileFilter)vf.get(i);
     if (eff.desc.startsWith(s)) {
        setFileFilter(eff);
        break;
     }
       }
    }
    */


    public void setRootDirectory(File f) {
        if (f != null && f.isDirectory()) {
            setCurrentDirectory(f);
        }

    }

}


