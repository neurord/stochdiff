package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruScrollingInfoPanel;
import org.catacomb.interlish.structure.Marketplace;


public class ScrollingInfoPanel extends Panel {


    public String text;

    public String scope;

    public int width;
    public int height;

    public String sources;

    public String action;
    public int maxlines;



    public DruPanel instantiatePanel() {
        if (height == 0) {
            height = 20;
        }
        DruScrollingInfoPanel ret = null;
        if (width > 10 && height > 10) {
            ret = new DruScrollingInfoPanel(text, width, height);
        } else {
            ret = new DruScrollingInfoPanel(text);
        }
        return ret;
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {

        DruScrollingInfoPanel drup = (DruScrollingInfoPanel)dp;


        drup.setEtchedBorder(ctx.getBg());

        Marketplace hub = ctx.getMarketplace(scope);

        //   E.info("sip registering on " + hub + " scope=" + scope);

        if (sources != null) {
            String[] sa = sources.split(",");
            for (int i = 0; i < sa.length; i++) {
                hub.addConsumer("Info", drup, sa[i].trim());
            }
        } else {
            hub.addConsumer("Info", drup, "*");
        }

    }

}
