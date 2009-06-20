package org.catacomb.interlish.structure;


public interface RowPeer {


    void initFromRow(Row table);

    void attachRow(Row table);

    Row getRow();

    void notifyObservers();


}
