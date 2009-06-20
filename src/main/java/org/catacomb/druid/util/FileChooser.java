package org.catacomb.druid.util;

import org.catacomb.druid.swing.DFileChooser;
import org.catacomb.interlish.service.AppPersist;

import java.io.File;
import java.util.HashMap;

import javax.swing.JFileChooser;



public class FileChooser {

    static FileChooser chooser;


    public static FileChooser getChooser() {
        if (chooser == null) {
            chooser = new FileChooser();
        }
        return chooser;
    }

    DFileChooser dFileChooser;

    HashMap<String, File> hmap;



    public FileChooser() {
        dFileChooser = new DFileChooser();

        hmap = new HashMap<String, File>();
    }



    public void addExtensionFilter(String exts, String desc) {
        dFileChooser.addExtensionFilter(exts, desc);
    }


    public File getFileToImport() {
        return getFile("import", "import");
    }


    public File getFileToOpen(String mode) {
        return getFile(mode, "open");
    }

    public File getFileToWrite(String mode, String ext, String desc) {
        addExtensionFilter(ext, desc);
        dFileChooser.setFilter(ext);
        return getFile(mode, "save");
    }

    public File getFileToWrite(String mode) {
        return getFile(mode, "save");
    }

    public void setDefaultFolderForMode(String mode, File fdir) {
        if (hmap.containsKey(mode)) {
            // leave as is;
        } else {
            hmap.put(mode, fdir);
        }
    }



    private void applyMode(String mode) {
        if (hmap.containsKey(mode)) {
            File fdir = hmap.get(mode);
            dFileChooser.setRootDirectory(fdir);

        } else {
            String pel = "LastDir" + mode;
            if (AppPersist.hasValueFor(pel)) {

                String path = AppPersist.getValueFor(pel);
                File fpar = new File(path);
                if (fpar.exists() && fpar.isDirectory()) {
                    dFileChooser.setRootDirectory(fpar);
                }
            }
        }
    }


    public File getFile(String mode, String approve) {
        applyMode(mode);

        dFileChooser.setApproveButtonText(approve);

        int retval = dFileChooser.showDialog(null, approve);
        File f = null;
        if (retval == JFileChooser.APPROVE_OPTION) {
            f = dFileChooser.getSelectedFile();
        }

        cacheDir(f, mode);
        return f;
    }

    private void cacheDir(File f, String mode) {
        if (f == null) {

        } else {
            File fpar = f;
            if (!f.isDirectory()) {
                fpar = f.getParentFile();
            }

            String path = fpar.getAbsolutePath();
            AppPersist.setValue("LastDir" + mode, path);

            hmap.put(mode, fpar);
        }
    }


    public File getDirectory(File fdir, String approve) {
        dFileChooser.setSelectDirectories();
        if (fdir != null) {
            // dFileChooser.setCurrentDirectory(new File(fdir.getParent()));
            dFileChooser.setSelectedFile(fdir);
        }

        File fret =  getDirectory(approve);
        cacheDir(fret, "default");
        return fret;
    }


    public File getDirectory(String mode, String approve) {
        applyMode(mode);

        File fret = getDirectory(approve);
        cacheDir(fret, mode);
        return fret;
    }


    private File getDirectory(String approve) {
        dFileChooser.setSelectDirectories();
        dFileChooser.setApproveButtonText(approve);
        int retval = dFileChooser.showDialog(null, approve);

        File f = null;
        if (retval == JFileChooser.APPROVE_OPTION) {
            f = dFileChooser.getSelectedFile();
        }

        if (f != null && !f.isDirectory()) {
            f = new File(f.getParent());
        }
        return f;
    }



    public File getFolder(File fdef) {
        return getDirectory(fdef, "select");
    }


}
