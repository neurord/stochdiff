package org.catacomb.interlish.structure;


public interface Structure {


    String getTypeName();

    Object get(String s);

    Object getStatic(String s);

}
