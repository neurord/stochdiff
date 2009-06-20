package org.catacomb.interlish.structure;


public interface TablePeer {


    void initFromTable(Table table);

    void attachTable(Table table);

    Table getTable();

    void removeChild(TablePeer tp);

    void notifyObservers();


}
