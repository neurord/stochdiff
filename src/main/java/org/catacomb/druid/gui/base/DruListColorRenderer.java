package org.catacomb.druid.gui.base;


import org.catacomb.druid.swing.DListCellRenderer;
import org.catacomb.druid.swing.DListColorRenderer;



public class DruListColorRenderer implements DruListCellRenderer {
    static final long serialVersionUID = 1001;


    DListColorRenderer dRenderer;

    public DruListColorRenderer() {
        dRenderer = new DListColorRenderer();
    }

    public DListCellRenderer getGUIPeer() {
        return dRenderer;
    }

}

