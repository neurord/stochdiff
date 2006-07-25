package org.textensor.xml;


public class Attribute {

    String name;
    String value;


    public Attribute(String s, String val) {
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
