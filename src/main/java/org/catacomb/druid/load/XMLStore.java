package org.catacomb.druid.load;


import java.util.ArrayList;

import org.catacomb.report.E;
import org.catacomb.druid.manifest.DecFile;
import org.catacomb.druid.manifest.DecManifest;

import java.io.File;
import java.util.HashMap;


public class XMLStore {

    private final static String MULTICODE = "___MULTI";

    private HashMap<String, String> shortToFullID;
    private HashMap<String, DecFile> fullIDToSource;

    private HashMap<String, ArrayList<String>> shortToFullMulti;


    public XMLStore() {
        shortToFullID = new HashMap<String, String>();
        fullIDToSource = new HashMap<String, DecFile>();
        shortToFullMulti = new HashMap<String, ArrayList<String>>();
    }



    public void addClasspathManifest(DecManifest manifest) {
        for (DecFile df : manifest.getFiles()) {
            df.setClasspathAccess();
            addSource(df);
        }
    }



    public void addFileSystemManifest(DecManifest manifest, File fdir) {
        String cproot = manifest.getRootPath();
        for (DecFile df : manifest.getFiles()) {
            df.setFileSystemAccess(fdir, cproot);
            addSource(df);
        }
    }


    public boolean containsSource(String fnm) {
        boolean ret = false;
        if (shortToFullID.containsKey(fnm) ||
                fullIDToSource.containsKey(fnm)) {
            ret = true;
        }
        return ret;
    }


    public boolean hasMultipleSources(String locator) {
        boolean ret = false;
        if (shortToFullID.containsKey(locator)) {
            String v = shortToFullID.get(locator);
            if (v.equals(MULTICODE)) {
                ret = true;
            }
        }
        return ret;
    }


    public ArrayList<DecFile> getSources(String locator) {
        ArrayList<DecFile> ret = new ArrayList<DecFile>();
        for (String path : shortToFullMulti.get(locator)) {
            ret.add(fullIDToSource.get(path));
        }
        return ret;
    }



    public DecFile getSource(String locator) {
        DecFile ret = null;

        if (shortToFullMulti.containsKey(locator)) {
            E.error("multiple possible sources for " + locator + " use getSources");
            return null;
        }

        if (shortToFullID.containsKey(locator)) {
            String path = shortToFullID.get(locator);
            ret = fullIDToSource.get(path);

        } else if (fullIDToSource.containsKey(locator)) {
            ret = fullIDToSource.get(locator);

        } else {
            E.error("cant find resource at " + locator);
            dumpStore();
        }
        return ret;
    }


    private void dumpStore() {
        E.info("Known names:");
        for (String sk : shortToFullID.keySet()) {
            E.info("   " + sk);
        }
        E.info("Known names:");
        for (String sk : shortToFullID.keySet()) {
            E.info("   " + sk);
        }
    }




    public void addSource(DecFile decFile) {
        String dfnm = decFile.getName();
        String fullid = decFile.getFullID();

        if (shortToFullID.containsKey(dfnm)) {
            if (shortToFullMulti.containsKey(dfnm)) {
                shortToFullMulti.get(dfnm).add(fullid);

            } else {
                ArrayList<String> al = new ArrayList<String>();
                al.add(shortToFullID.get(dfnm));
                al.add(fullid);
                shortToFullMulti.put(dfnm, al);

                shortToFullID.put(dfnm, MULTICODE);
            }

        } else {
            shortToFullID.put(dfnm, fullid);
        }
        fullIDToSource.put(fullid, decFile);
    }



    public void newSourceFile(File f, File rootFolder) {
        DecFile df = new DecFile(f, rootFolder);
        addSource(df);
    }


}
