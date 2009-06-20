package org.catacomb.druid.gui.base;

import org.catacomb.druid.gui.edit.DruMenu;
import org.catacomb.druid.swing.DMenuBar;




public class DruMenuBar {
    static final long serialVersionUID = 1001;

    String id;

    DMenuBar dMenuBar;

    public DruMenuBar(String s) {
        dMenuBar = new DMenuBar();
        id = s;
    }


    public DMenuBar getGUIPeer() {
        return dMenuBar;
    }

    public String getID() {
        return id;
    }

    public String toString() {
        return "DruMenuBar " + id;
    }


    public void addMenu(DruMenu am) {
        dMenuBar.add(am.getGUIPeer());
    }


}
