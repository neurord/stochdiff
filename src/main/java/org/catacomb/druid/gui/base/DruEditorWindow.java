package org.catacomb.druid.gui.base;


import org.catacomb.interlish.structure.FrameShowable;


public class DruEditorWindow extends DruRoot implements FrameShowable {

    DruFrame mainFrame;



    public void setMainFrame(DruFrame df) {
        mainFrame = df;
    }

    public DruFrame getMainFrame() {
        return mainFrame;
    }


    public void pack() {
        mainFrame.pack();
    }

    public int[] getSize() {
        return mainFrame.getIntArraySize();
    }


    public void setLocation(int x, int y) {
        mainFrame.setLocation(x, y);
    }

    public void show() {
        mainFrame.packIfNecessary();
        mainFrame.setVisible(true);
    }

    public void hide() {
        mainFrame.setVisible(false);
    }

    public void toFront() {
        mainFrame.toFront();
    }

    public int[] getLocation() {
        return mainFrame.getLocation();
    }

}
