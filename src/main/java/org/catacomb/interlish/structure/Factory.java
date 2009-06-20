package org.catacomb.interlish.structure;


public interface Factory {


    Object make(String s);


    void populate(Object obj, Element elt);

}
