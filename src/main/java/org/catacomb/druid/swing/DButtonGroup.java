package org.catacomb.druid.swing;

import java.util.Collections;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;


public class DButtonGroup extends ButtonGroup {

    static final long serialVersionUID = 1001;


    public String getSelectedName() {
        String ret = null;
        ButtonModel bm = getSelection();
        if (bm != null) {
            ret = bm.getActionCommand();
        }
        return ret;
    }


    public void setSelectedName(String s) {
        if (s == null)
            return;
        ButtonModel bmsel = null;

        for (AbstractButton ab : Collections.list(getElements())) {
            if (s.equals(ab.getActionCommand())) {
                bmsel = ab.getModel();
                break;
            }
        }

        if (bmsel != null) {
            setSelected(bmsel, true);
        }
    }



    public boolean hasElementCalled(String s) {
        boolean bok = false;

        if (s != null) {
            for (AbstractButton ab : Collections.list(getElements())) {
                if (s.equals(ab.getActionCommand())) {
                    bok = true;
                    break;
                }
            }
        }
        return bok;
    }

}
