package org.catacomb.serial.xml;

import org.catacomb.interlish.structure.Attribute;


public class NamedString implements Attribute {

    String name;
    String value;


    public NamedString(String s, String val) {
        name = s;
        value = val;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

}
