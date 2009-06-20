package org.catacomb.interlish.structure;


public interface ChangeTime {


    void now();

    int getValue();

    boolean isAfter(ChangeTime ct);

    boolean isBefore(ChangeTime ct);

}
