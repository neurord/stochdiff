package org.catacomb.druid.swing;

import javax.swing.DefaultListSelectionModel;

public class ToggleSelectionModel extends DefaultListSelectionModel {


    public void setSelectionInterval(int index0, int index1) {
        if (isSelectedIndex(index0)) {
            super.removeSelectionInterval(index0, index1);
        } else {
            super.addSelectionInterval(index0, index1);
        }
    }


}

