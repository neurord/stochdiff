package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruInfoPanel;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.interlish.structure.Marketplace;
import org.catacomb.report.E;


public class InfoPanel extends Panel {


    public String text;

    public int width;
    public int height;

    public String sources;



    public DruPanel instantiatePanel() {
        DruInfoPanel ret = null;

        if (width > 60 && height > 20) {
            ret = new DruInfoPanel(text, width, height);

        } else {
            if (width > 0 && height > 0) {
                E.warning("width, height too small to be useful " + width + " " + height);
            }
            ret = new DruInfoPanel(text);
        }
        return ret;
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {
        if (height == 0) {
            height = 20;
        }
        DruInfoPanel drup = (DruInfoPanel)dp;

        Marketplace hub = ctx.getMarketplace();

        if (sources != null) {
            if (sources.equals("none") || sources.length() == 0) {

            } else {
                if (sources.equals("*")) {
                    hub.addReceiver("Info", drup, "*");

                } else {
                    String[] sa = sources.split(",");
                    for (int i = 0; i < sa.length; i++) {
                        hub.addReceiver("Info", drup, sa[i].trim());
                    }
                }
            }
        }

    }

}
