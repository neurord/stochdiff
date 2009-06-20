package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.base.DruScrollingHTMLPanel;


public class ScrollingHTMLPanel extends Panel {

    public String content;

    public String show;

    public String stylesheet;

    public boolean preformat;


    public DruPanel instantiatePanel() {
        return new DruScrollingHTMLPanel();
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {
        DruScrollingHTMLPanel dhp = (DruScrollingHTMLPanel)dp;

        if (preformat) {
            dhp.setPreformat(true);
        }

        if (content != null) {
            dhp.setText(content);
        }


        if (show != null) {
            ctx.getMarketplace().addConsumer("Page", dhp, show);
        }

        if (stylesheet != null) {
            dhp.setStylesheetPath(stylesheet);
        }

    }



}
