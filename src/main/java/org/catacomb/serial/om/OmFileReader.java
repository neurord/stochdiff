package org.catacomb.serial.om;

import java.io.File;

import org.catacomb.report.E;
import org.catacomb.serial.Archivist;
import org.catacomb.serial.Deserializer;
import org.catacomb.util.FileUtil;


public class OmFileReader extends OmWriter {

    protected File file;


    public OmFileReader(File f) {
        file = f;
    }


    public File getFile() {
        return file;
    }

    public File getResourceDir() {
        /*
        String fnm = FileUtil.getRootName(file);
        String resnm = fnm + "_resources";
        File fparent = file.getParentFile();
        File fres = new File(fparent, resnm);
        if (fres.exists()) {
           // OK;
        } else {
           fres.mkdir();
        }
        return fres;
        */
        return file.getParentFile();
    }



    public void writeResource(Object oext, String resnm) {
        File fres = new File(getResourceDir(), resnm);
        Archivist.storeXMLOnly(oext, fres);
    }



    public Object readResource(String resnm) {
        Object ret = null;

        File f = new File(getResourceDir(), resnm);
        if (f.exists()) {

            String ftxt = FileUtil.readStringFromFile(f);
            ret = Deserializer.deserialize(ftxt);

        } else {
            E.error("no such resource file " + f);
        }
        return ret;
    }

}
