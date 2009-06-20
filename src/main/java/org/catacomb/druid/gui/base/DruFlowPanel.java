package org.catacomb.druid.gui.base;

import org.catacomb.report.E;


public class DruFlowPanel extends DruPanel {

    static final long serialVersionUID = 1001;

    public final static int LEFT = 9;
    public final static int CENTER = 10;
    public final static int RIGHT = 11;


    public DruFlowPanel() {
        this(CENTER, 0, 0);
    }


    public DruFlowPanel(int dir) {
        this(dir, 0, 0);
    }


    public DruFlowPanel(int dir, int dx, int dy) {
        super();
        applyLayout(dir, dx, dy);
    }



    public void applyLayout(int dir, int dx, int dy) {
        if (dir == LEFT) {
            setFlowLeft(dx, dy);

        } else if (dir == RIGHT) {
            setFlowRight(dx, dy);

        } else if (dir == CENTER) {
            setFlowCenter(dx, dy);
        } else {
            E.error("unrecognized direction " + dir);
            setFlowCenter(dx, dy);
        }
    }


}
