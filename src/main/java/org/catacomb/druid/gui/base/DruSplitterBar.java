package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.split.DSplitterBar;


public class DruSplitterBar {


    DSplitterBar dSplitterBar;



    public DruSplitterBar() {
        dSplitterBar = new DSplitterBar();
    }


    public DSplitterBar getGUIPeer() {
        return dSplitterBar;
    }

}
