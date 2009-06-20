package org.catacomb.druid.blocks;

import org.catacomb.druid.build.Context;
import org.catacomb.druid.build.GUIPath;
import org.catacomb.druid.gui.base.DruPanel;
import org.catacomb.druid.gui.edit.DruHTMLPanel;
import org.catacomb.xdoc.HTMLPage;


public class HTMLPanel extends Panel {

    public String content;

    public String show;

    public String linkAction;

    public boolean preformat;


    public HTMLPanel() {
        super();
        content = "";
    }

    public DruPanel instantiatePanel() {
        return  new DruHTMLPanel();
    }


    public void populatePanel(DruPanel dp, Context ctx, GUIPath gpath) {
        DruHTMLPanel dhp = (DruHTMLPanel)dp;

        if (preformat) {
            dhp.setPreformatted(true);
        }

        if (content != null) {
            dhp.setPage(new HTMLPage(content, HTMLPage.DASSIE_TEXT));
        }


        if (show != null) {
            ctx.getMarketplace().addConsumer("Page", dhp, show);
        }

        if (linkAction != null) {
            dhp.setLinkAction(linkAction);
        }

    }



}
