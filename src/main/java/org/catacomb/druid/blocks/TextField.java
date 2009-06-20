
package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruTextField;
import org.catacomb.report.E;


public class TextField extends Panel {


    public String label;

    public String store;

    public String action;

    public int width;

    public String report;

    public boolean able = true;


    public TextField() {
    }


    public TextField(String slab) {
        label = slab;
        action = label;
    }


    public DruPanel instantiatePanel() {
        return new DruTextField(action, width);
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruTextField dtf = (DruTextField)dp;



        if (report != null) {
            E.deprecate();
        }

        if (action != null) {
            dtf.setReturnAction(action);
        }

        if (!able) {
            dtf.able(false);
        }


    }

}
