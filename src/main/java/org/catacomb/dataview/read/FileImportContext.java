
package org.catacomb.dataview.read;


import org.catacomb.dataview.read.Importer;


import java.io.File;


public class FileImportContext extends FUImportContext {

    File baseFile;
    File baseDir;


    public FileImportContext(File f) {
        baseFile = f;
        baseDir = f.getParentFile();
    }


    public boolean hasRelative(String sr) {
        File fr = new File(baseDir, sr);
        return (fr.exists());
    }



    public File getRelativeFile(String sr) {
        File fr = new File(baseDir, sr);
        return fr;
    }


    public ContentReader getRelativeReader(String sr) {
        File file = getRelativeFile(sr);
        return Importer.getReader(file);
    }


    public String getExtensionRelativeName(String ext) {
        return getReExtendedName(baseFile.getName(), ext);
    }



}
