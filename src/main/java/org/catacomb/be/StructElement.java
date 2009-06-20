package org.catacomb.be;


public class StructElement {

    String id;
    String type;


    public StructElement(String sid, String styp) {
        id = sid;
        type = styp;
    }

    public String getID() {
        return id;
    }

    public String getType() {
        return type;
    }

}
