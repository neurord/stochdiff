
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruToggleListPanel;


public class ToggleList extends Panel {


    public String action;
    public int nrow;




    public DruPanel instantiatePanel() {

        if (nrow == 0) {
            nrow = 10;
        }
        DruToggleListPanel drup = new DruToggleListPanel(nrow);
        drup.setAction(action);



        return drup;
    }




    @Override
    public void populatePanel(DruPanel drup, Context ctx, GUIPath gpath) {

    }

}


