package org.catacomb.interlish.structure;


public interface TouchTime {

    int time();

    boolean isAfter(TouchTime tt);

    boolean isBefore(TouchTime tt);

    void now();


}
