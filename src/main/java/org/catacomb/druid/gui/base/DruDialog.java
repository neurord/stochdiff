
package org.catacomb.druid.gui.base;

import java.awt.Point;

import org.catacomb.druid.swing.DDialog;
import org.catacomb.interlish.structure.ActionRelay;
import org.catacomb.interlish.structure.ActionSource;
import org.catacomb.interlish.structure.Dialog;



public class DruDialog implements ActionSource, Dialog {
    static final long serialVersionUID = 1001;

    // DruPanel druPanel;

    String id;

    // ActionRelay actionRelay;

    DDialog dDialog;


    public DruDialog(DruFrame druf, String s) {
        dDialog = new DDialog(druf.getGUIPeer(), s);
    }


    public void setID(String s) {
        id = s;
    }


    public String getID() {
        return id;
    }

    public void setActionRelay(ActionRelay ac) {
        // actionRelay = ac;
    }


    public void setDruPanel(DruPanel drup) {
        // druPanel = drup;
        dDialog.setPanel(drup.getGUIPeer());

    }

    public void setModal(boolean b) {
        dDialog.setModal(b);
    }

    public void open() {
        dDialog.open();
    }


    public void close() {
        dDialog.close();
    }
    public int[] getLocation() {
        Point p = dDialog.getLocation();
        int[] ret = {(int)(p.getX()), (int)(p.getY())};
        return ret;
    }

    public int[] getIntArraySize() {
        return dDialog.getIntArraySize();
    }


    public void setLocation(int x, int y) {
        dDialog.setLocation(x, y);
    }


    public void pack() {
        dDialog.pack();
    }


    public void setVisible(boolean b) {
        dDialog.setVisible(b);
    }


    public boolean isShowing() {
        boolean ret = dDialog.isShowing();
        return ret;
    }


    public void toFront() {
        dDialog.toFront();
    }


}
