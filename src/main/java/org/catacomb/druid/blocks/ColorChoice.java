
package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruColorChoice;



public class ColorChoice extends Panel {


    public String label;
    public String action;

    public String store;



    public DruPanel instantiatePanel() {
        return new DruColorChoice(label, action);
    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {


        if (title == null) {
            title="Color Choice";
        }


    }


}
