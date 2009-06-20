package org.catacomb.druid.swing.split;

import java.awt.event.*;

class SplitterBarMouseListener extends MouseAdapter {
    private DSplitterBar s;

    public SplitterBarMouseListener(DSplitterBar s) {
        this.s = s;
    }
    public void mouseEntered(MouseEvent e) {
        s.mouseEnter(e);
    }
    public void mouseExited(MouseEvent e) {
        s.mouseExit(e);
    }
    public void mouseReleased(MouseEvent e) {
        s.mouseRelease(e);
    }
}