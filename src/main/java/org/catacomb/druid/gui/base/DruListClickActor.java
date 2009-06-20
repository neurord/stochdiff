package org.catacomb.druid.gui.base;

import org.catacomb.druid.gui.edit.DruListPanel;


public class DruListClickActor {

    int xmin;
    int xmax;
    String action;



    public DruListClickActor(int xm, int xx, String act) {
        xmin = xm;
        xmax = xx;
        action = act;
    }

    public void clicked(int x, DruListPanel drulp) {
        if (x > xmin && x < xmax) {
            drulp.performAction(action);
        }
    }

}
