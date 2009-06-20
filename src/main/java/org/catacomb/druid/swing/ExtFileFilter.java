package org.catacomb.druid.swing;

import java.io.File;

import javax.swing.filechooser.FileFilter;


class ExtFileFilter extends FileFilter {

    String extensions;
    String description;

    public ExtFileFilter(String se, String sd) {
        extensions = ":" + se + ":";
        description = sd;

    }

    public boolean accept(File f) {
        String s = f.getName();
        boolean ok = false;
        if (f.isDirectory()) {
            ok = true;
        }
        int l = s.lastIndexOf(".");
        if (l > 0) {
            String ext = s.substring(l+1, s.length());
            if (extensions.indexOf(":" + ext + ":") >= 0) {
                ok = true;
            }
        }
        return ok;
    }

    public String getDescription() {
        return description;

    }
}

