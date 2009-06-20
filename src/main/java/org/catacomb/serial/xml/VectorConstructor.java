package org.catacomb.serial.xml;

import org.catacomb.interlish.structure.Attribute;
import org.catacomb.interlish.structure.Constructor;
import org.catacomb.report.E;



public class VectorConstructor implements Constructor {


    public Object newInstance(String s) {
        return new NVPair(s);
    }


    public Object getChildObject(Object parent, String name, Attribute[] atta) {
        return new NVPair(name);
    }


    public void applyAttributes(Object obj, Attribute[] atta) {
        // MISSING but can get rid of whole class?
    }

    public void appendContent(Object obj, String s) {

    }


    public boolean setAttributeField(Object ob, String sf, String arg) {
        return setField(ob, sf, arg);
    }


    public boolean setField(Object parent, String fieldName, Object child) {
        NVPair nvparent = (NVPair)parent;

        nvparent.addNVPair(new NVPair(fieldName, child));
        return true;
    }


    public Object getField(Object parent, String fieldName) {
        return new NVPair(fieldName);
    }


    public void setIntFromStatic(Object ret, String id, String sv) {
        E.missing();
    }


}
