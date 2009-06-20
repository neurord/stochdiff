package org.catacomb.serial.om;

import org.catacomb.interlish.structure.Attribute;

public class OmAttribute implements Attribute {



    public String name;
    public String value;


    public OmAttribute(String fnm, String fv) {
        name = fnm;
        value = fv;
    }

    public OmAttribute(String fnm, boolean b) {
        name = fnm;
        if (b) {
            value = "true";
        } else {
            value = "false";
        }
    }


    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String v) {
        value = v;
    }

}
