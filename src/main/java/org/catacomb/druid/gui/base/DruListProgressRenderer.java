package org.catacomb.druid.gui.base;


import org.catacomb.druid.swing.DListCellRenderer;
import org.catacomb.druid.swing.DListProgressRenderer;



public class DruListProgressRenderer implements DruListCellRenderer {
    static final long serialVersionUID = 1001;

    DListProgressRenderer dRenderer;

    public DruListProgressRenderer() {
        dRenderer = new DListProgressRenderer();
    }

    public DListCellRenderer getGUIPeer() {
        return dRenderer;
    }



}

