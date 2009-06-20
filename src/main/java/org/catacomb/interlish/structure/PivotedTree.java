package org.catacomb.interlish.structure;


public interface PivotedTree extends Tree {

    String[] getPivotNames();

    void repivot(String s);

}
