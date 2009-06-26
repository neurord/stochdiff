
package org.catacomb.dataview.read;



import org.catacomb.interlish.resource.ImportContext;

import java.io.File;
import java.net.URL;



public class FUImportContext implements ImportContext {




    public static FUImportContext makeContext(Object obj) {
        FUImportContext ret = null;
        if (obj == null) {
            ret = new FUImportContext();

        } else if (obj instanceof File) {
            ret = new FileImportContext((File)obj);


        } else if (obj instanceof URL) {
            ret = new URLImportContext((URL)obj);
        }

        return ret;
    }



    public Object getRelative(String rp) {
        return null;
    }


    public File getSourceFile() {
        return null;
    }

    public boolean hasRelative(String sr) {
        return false;
    }

    public ContentReader getRelativeReader(String sr) {
        return null;
    }
    public File getRelativeFile(String sr) {
        return null;
    }

    public String getExtensionRelativeName(String ext) {
        return null;
    }


    public String getReExtendedName(String orig, String ext) {
        String ret = null;
        String mnm = orig; // getMainName();
        if (mnm != null) {
            ret = mnm;
            if (ret.indexOf(".") > 0) {
                ret = ret.substring(0, ret.lastIndexOf("."));
            }
            ret = ret + ext;
        }
        return ret;
    }


}
