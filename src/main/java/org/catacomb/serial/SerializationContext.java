
package org.catacomb.serial;


import org.catacomb.interlish.structure.ElementFactory;
import org.catacomb.serial.om.OmElementFactory;


import java.util.HashMap;


public class SerializationContext {


    HashMap<String, Object> referentHM;

    String currentPackage;

    // values for recurse;
    final static int PRIMITIVES_ONLY = 1;
    final static int ALL = 2;

    int recurse = ALL;

    OmElementFactory eltfac;



    public SerializationContext() {
        referentHM = new HashMap<String, Object>();
        recurse = ALL;
    }


    public void setPrimitivesOnly() {
        recurse = PRIMITIVES_ONLY;
    }


    public ElementFactory getElementFactory() {
        if (eltfac == null) {
            eltfac = new OmElementFactory();
        }
        return eltfac;
    }


    public boolean acceptsReferents() {
        return true;
    }


    public void addReferent(String hcode, Object val) {
        if (referentHM == null) {
            referentHM = new HashMap<String, Object>();
        }
        referentHM.put(hcode, val);
    }



    public boolean recurseAll() {
        return (recurse == ALL);
    }


    public HashMap<String, Object> getHashMap() {
        return referentHM;
    }


    public boolean shouldWritePackage(String s) {
        boolean ret = false;
        if (currentPackage != null && currentPackage.equals(s)) {
            // leave as is;

        } else {
            currentPackage = s;
            ret = true;
        }
        return ret;
    }

}
