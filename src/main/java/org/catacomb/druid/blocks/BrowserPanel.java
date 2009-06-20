package org.catacomb.druid.blocks;


import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruBrowserPanel;
import org.catacomb.druid.gui.base.DruPanel;



public class BrowserPanel extends Panel {

    public String content;

    public String show;



    public DruPanel instantiatePanel() {
        return new DruBrowserPanel(content);
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {
        DruBrowserPanel dhp = (DruBrowserPanel)dp;

        dhp.setEtchedBorder(ctx.getBg());

        if (show != null) {
            ctx.getMarketplace().addConsumer("Page", dhp, "state");
        }
    }


}
