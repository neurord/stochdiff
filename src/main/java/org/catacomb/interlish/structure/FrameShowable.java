package org.catacomb.interlish.structure;


public interface FrameShowable {

    int[] getSize();

    int[] getLocation();

    void setLocation(int x, int y);

    void pack();

    void show();

    void hide();

    void toFront();

}
