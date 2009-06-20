package org.catacomb.interlish.structure;


public final class Relationship {

    String type;
    Related target;


    public Relationship(Related tgt, String typ) {
        type = typ;
        target = tgt;
    }


    public String getType() {
        return type;
    }

    public Related getTarget() {
        return target;
    }

}
