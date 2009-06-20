
package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruLoggedButton;



public class LoggedButton extends Panel {


    public String label;
    public String action;


    public LoggedButton() {
    }



    public LoggedButton(String slab) {
        label = slab;
        action = label;
    }


    public DruPanel instantiatePanel() {
        return new DruLoggedButton(label);

    }

    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {
        DruLoggedButton drup = (DruLoggedButton)dp;

        ctx.getMarketplace().addProducer("LogMessage", drup, "default");
    }


}
