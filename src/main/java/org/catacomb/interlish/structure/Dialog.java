package org.catacomb.interlish.structure;


public interface Dialog extends Openable, Closable {

    void setModal(boolean b);

    boolean isShowing();

    int[] getLocation();

}
