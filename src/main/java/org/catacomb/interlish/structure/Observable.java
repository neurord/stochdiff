package org.catacomb.interlish.structure;


public interface Observable {


    void addObserver(Observer obs);

    void removeObserver(Observer obs);

}
