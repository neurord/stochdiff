package org.catacomb.druid.util.examples;


import org.catacomb.interlish.structure.Related;
import org.catacomb.interlish.structure.Relationship;

import java.util.ArrayList;


public class TreeItem implements Related {

    ArrayList<Relationship> relationships;

    String name;


    public TreeItem(String sn) {
        name = sn;
        relationships = new ArrayList<Relationship>();
    }

    public String toString() {
        return name;
    }


    public void addRelationship(Related tgt, String rel) {
        relationships.add(new Relationship(tgt, rel));
    }


    public Relationship[] getRelationships() {
        return (relationships.toArray(new Relationship[0]));
    }

}
