package org.catacomb.interlish.structure;


public interface SettableStructure extends Structure {

    void set(String s, Object val);

    void setContext(String s, Object val);

}
