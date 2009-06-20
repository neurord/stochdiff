package org.catacomb.druid.manifest;

import org.catacomb.report.E;
import org.catacomb.util.FileUtil;

import java.io.File;


public class DecFile {

    public String directory;
    public String name;
    public String extension;

    public static final int CLASSPATH = 0;
    public static final int FILE_SYSTEM = 1;
    private int p_accessMethod;
    private File p_rootFolder;

    private String p_fullID;
    private String p_cachedText;
    private Object p_cachedObject;



    public DecFile() {
    }


    public DecFile(String psf, String nm) {
        directory = psf;
        name = nm;
        cutName();
    }


    public DecFile(File f, File rtFolder) {
        p_accessMethod = FILE_SYSTEM;
        p_rootFolder = rtFolder;

        name= f.getName();
        directory = FileUtil.getRelativeDirectory(f, rtFolder);

        cutName();
    }




    public DecFile(String fullPath) {
        if (fullPath.startsWith("file:")) {
            String sp = fullPath.substring(5, fullPath.length());
            File f = new File(sp);
            directory = f.getParentFile().getAbsolutePath();
            name = f.getName();
            cutName();
            setFileSystemAccess(new File("/"), "");
        } else {
            E.missing();
        }
    }


    public String toString() {
        return "decFile dir=" + directory + " name=" + name + " ext=" + extension +
               " access=" + p_accessMethod;

    }


    private void cutName() {
        if (name.endsWith(".xml")) {
            name = name.substring(0, name.length() - 4);
            extension = "xml";
        } else {
            extension = "";
            E.error(" - DecFile in DecManifest only handles " + "extension .xml  so far");
        }
    }


    public String getName() {
        return name;
    }


    public String getDirectory() {
        return directory;
    }


    public String getFullID() {
        if (p_fullID == null) {
            p_fullID = makeFullID();
        }
        return p_fullID;
    }


    private String makeFullID() {
        if (directory == null) {
            directory = "";
            E.error("null directory in DecFile");
        }
        String sret = null;
        if (directory.length() > 0) {
            if (directory.endsWith("/")) {
                sret = directory + name;
            } else {
                sret = directory + "/" + name;
            }
        } else {
            sret = name;
        }
        sret = sret.replaceAll("/", ".");
        return sret;
    }


    public void setFileSystemAccess(File fdir, String cproot) {
        directory = cproot + directory;
        p_accessMethod = FILE_SYSTEM;
        p_rootFolder = fdir;
    }


    public void setClasspathAccess() {
        p_accessMethod = CLASSPATH;
    }


    public boolean inClasspath() {
        return (p_accessMethod == CLASSPATH);
    }


    public boolean inFileSystem() {
        return (p_accessMethod == FILE_SYSTEM);
    }


    public void setCachedObject(Object obj) {
        p_cachedObject = obj;
    }


    public boolean hasCachedObject() {
        return (p_cachedObject != null);
    }


    public Object getCachedObject() {
        return p_cachedObject;
    }


    public void setCachedText(String s) {
        p_cachedText = s;
    }


    public boolean hasCachedText() {
        return (p_cachedText != null);
    }


    public String getCachedText() {
        return p_cachedText;
    }


    public void clearObjectCache() {
        p_cachedObject = null;
    }


    public void clearTextCache() {
        p_cachedText = null;
    }


    public File getSourceFile() {
        String fnm = name + "." + extension;

        File fdir = null;
        if (p_rootFolder != null) {
            fdir = new File(p_rootFolder, directory);
        } else {
            fdir = new File(directory);
        }
        return new File(fdir, fnm);
    }


    public String getPath() {
        String sd = directory;
        if (sd == null || sd.length() == 0) {
            sd = "";
        } else {
            sd = directory + "/'";
        }
        return sd + name + "." + extension;

    }



}
