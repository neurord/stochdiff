package org.catacomb.interlish.structure;


public interface Element extends Named, Elemented, Attributed {

    boolean hasText();

    String getText();

    String serialize();

}
