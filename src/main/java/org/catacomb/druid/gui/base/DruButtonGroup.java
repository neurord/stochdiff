
package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DButtonGroup;


public class DruButtonGroup {
    //implements NameSelectable {
    static final long serialVersionUID = 1001;



    DButtonGroup dButtonGroup;


    public DruButtonGroup(String s) {
        dButtonGroup = new DButtonGroup();
    }


    public void select(String s) {
        dButtonGroup.setSelectedName(s);
    }

}
