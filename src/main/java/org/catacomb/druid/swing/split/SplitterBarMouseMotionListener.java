package org.catacomb.druid.swing.split;

import java.awt.event.*;

class SplitterBarMouseMotionListener extends MouseMotionAdapter {
    private DSplitterBar s;

    public SplitterBarMouseMotionListener(DSplitterBar s) {
        this.s = s;
    }
    public void mouseDragged(MouseEvent e) {
        s.mouseDrag(e);
    }
}