package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruDropBox;



public class DropBox extends Panel {


    public String action;

    public DruPanel instantiatePanel() {
        prefHeight = 30;
        if (prefWidth <= 10) {
            prefWidth = 100;
        }

        return new DruDropBox();
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruDropBox dhp = (DruDropBox)dp;

        if (action != null) {
            dhp.setAction(action);
        }


        dhp.setEtchedBorder(ctx.getBg());

    }


}
