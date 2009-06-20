package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruLogPanel;
import org.catacomb.interlish.structure.Marketplace;


public class LogPanel extends Panel {

    public String text;

    public int height;

    public String scope;

    public String sources;



    public DruPanel instantiatePanel() {
        if (height == 0) {
            height = 20;
        }
        return new DruLogPanel(height);
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruLogPanel drup = (DruLogPanel)dp;

        Marketplace hub = ctx.getMarketplace(scope);

        if (sources != null) {
            String[] sa = sources.split(",");
            for (int i = 0; i < sa.length; i++) {
                hub.addConsumer("Log", drup, sa[i].trim());
            }
        } else {
            hub.addConsumer("Log", drup, "default");
        }

    }

}
