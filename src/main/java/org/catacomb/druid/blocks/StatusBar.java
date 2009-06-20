package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruStatusBar;
import org.catacomb.interlish.structure.Marketplace;


public class StatusBar extends Panel {

    public String text;

    public DruPanel instantiatePanel() {
        return  new DruStatusBar();

    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruStatusBar drup = (DruStatusBar)dp;
        if (text != null) {
            drup.showStatus(text);
        }


        Marketplace hub = ctx.getMarketplace();

        if (id == null || id.length() == 0) {
            hub.addReceiver("Status", drup, "status");

        } else {
            hub.addReceiver("Status", drup, id);
        }
    }





}
