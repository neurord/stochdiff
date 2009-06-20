

package org.catacomb.druid.gui.base;


import org.catacomb.interlish.structure.Dialog;
import org.catacomb.interlish.structure.FrameShowable;


public class DruAutonomousDialog extends DruRoot
    implements FrameShowable, Dialog {


    DruDialog druDialog;



    public void setDialog(DruDialog dd) {
        druDialog = dd;
        //      addRoot(dd);
    }

    public int[] getSize() {
        return druDialog.getIntArraySize();
    }

    public void setLocation(int x, int y) {
        druDialog.setLocation(x, y);
    }

    public void pack() {
        druDialog.pack();
    }

    public void show() {
        druDialog.setVisible(true);
    }

    public void hide() {
        druDialog.setVisible(false);
    }


    public void setModal(boolean b) {
        druDialog.setModal(b);
    }


    public void open() {
        show();
    }


    public void close() {
        hide();
    }

    public boolean isShowing() {
        return druDialog.isShowing();
    }

    public void toFront() {
        druDialog.toFront();

    }

    public int[] getLocation() {
        return druDialog.getLocation();
    }



}

