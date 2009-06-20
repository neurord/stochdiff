package org.catacomb.serial.state;

import org.catacomb.serial.om.OmElement;



public class TypeClass extends OmElement {

    public String catalogID;


    public TypeClass(String s, String typeName, int id) {
        super(s);
        addAttribute("typeName", "" + typeName);
        addAttribute("catalogID", "" + id);
    }


    public void addTypeInstance(TypeInstance ti) {
        addElement(ti);
    }

    public boolean hasInstances() {
        return hasElements();
    }

}
