
package org.catacomb.dataview.read;

import org.catacomb.report.E;


import java.io.File;


public class JarImportContext extends FUImportContext {


    FUImportContext rootContext;

    CustomJarReader customJarReader;


    public JarImportContext(FUImportContext fic) {
        rootContext = fic;
    }

    public void setJarReader(CustomJarReader cjr) {
        customJarReader = cjr;
    }

    public boolean hasRelative(String sr) {
        return customJarReader.hasRelative(sr);
    }


    public Object getRelative(String rp) {
        return customJarReader.getRelative(rp);
    }


    public File getRelativeFile(String sr) {
        E.missing();
        return null; // MISSING
    }


    public ContentReader getRelativeReader(String sr) {
        E.error("get rel reader returns null in jar import context");

        return null; // customJarReader.getRelativeReader(sr);
    }


    public String getExtensionRelativeName(String ext) {
        return null;
    }



}
