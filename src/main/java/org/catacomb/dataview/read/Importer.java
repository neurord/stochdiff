package org.catacomb.dataview.read;


import org.catacomb.util.FileUtil;
import org.catacomb.util.NetUtil;


import java.io.File;
import java.net.URL;


public class Importer {


    static ResourceImporter resourceImporter;



    public static ResourceImporter getResourceImporter() {
        if (resourceImporter == null) {
            resourceImporter = new ResourceImporter();
        }
        return resourceImporter;
    }



    public static Object importFile(File f) {
        return (getReader(f).getMain());
    }


    public static Object importURL(URL u) {
        return (getReader(u).getMain());
    }



    public static ContentReader getReader(byte[] ba, FUImportContext ctxt) {
        return getResourceImporter().getReader(ba, ctxt);
    }




    public static ContentReader getReader(File f) {
        byte[] ba = FileUtil.readBytes(f);
        FUImportContext ctxt = FUImportContext.makeContext(f);
        return getResourceImporter().getReader(ba, ctxt);
    }




    public static ContentReader getReader(URL u) {
        byte[] ba = NetUtil.readBytes(u);
        FUImportContext ctxt = FUImportContext.makeContext(u);
        return getResourceImporter().getReader(ba, ctxt);
    }



}
