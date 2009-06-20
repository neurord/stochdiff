package org.catacomb.druid.gui.base;

import org.catacomb.druid.gui.edit.DruButton;
import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.druid.swing.DHTMLPane;
import org.catacomb.druid.swing.DScrollPane;
import org.catacomb.interlish.structure.Page;
import org.catacomb.interlish.structure.PageDisplay;
import org.catacomb.interlish.structure.PageSupplier;
import org.catacomb.interlish.util.JUtil;
import org.catacomb.xdoc.HTMLPage;
import org.catacomb.xdoc.XdocBase;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;


public class DruBrowserPanel extends DruPanel implements PageDisplay {
    static final long serialVersionUID = 1001;

    DScrollPane dsp;

    String htmlTemplate;

    DHTMLPane htmlPane;
    DruFlowPanel dlfp;

    DruButton bprevious;
    DruButton bnext;



    public DruBrowserPanel() {
        init();

        HTMLPage hpage = new HTMLPage();
        hpage.loadDefault();

        showPage(hpage);
    }


    public DruBrowserPanel(String s) {
        init();

        HTMLPage hpage = new HTMLPage();
        if (s != null && s.length() > 3) {
            hpage.wrapText(s);
        } else {
            hpage.loadDefault();
        }

        showPage(hpage);
    }


    public DruBrowserPanel(HTMLPage hpage) {
        init();
        showPage(hpage);
    }


    public void init() {
        setBorderLayout(0, 0);

        DruActionRelay relay = new DruActionRelay(this);

        dsp = new DScrollPane();
        dsp.setVerticalScrollBarAlways();


        addDComponent(dsp,  DBorderLayout.CENTER);


        dlfp = new DruFlowPanel();

        bprevious = new DruButton("back");
        bprevious.setActionRelay(relay);

        bnext = new DruButton("next");
        bnext.setActionRelay(relay);

        dlfp.addPanel(bprevious);
        dlfp.addPanel(bnext);

        addPanel(dlfp, DBorderLayout.NORTH);


        htmlPane = new DHTMLPane();
        htmlPane.setEditable(false);
        htmlPane.setDefaultStyleSheet();

        dsp.setViewportView(htmlPane);
    }



    // EXTEND need separate handler class to receive requests and deliver pages; Should register
    // as sucn with htnlPane.
    public void setPageSupplier(PageSupplier ps) {

        DruLinkHandler linkHandler = new DruLinkHandler(ps, this);

        htmlPane.setHyperlinkHandler(linkHandler);
    }



    public void showBottom() {
        dsp.scrollToBottom();
    }


    public void showPage(Page page) {
        htmlPane.showPage(page);
    }



    public String getHTMLTemplate() {
        if (htmlTemplate == null) {
            htmlTemplate = JUtil.getRelativeResource(new XdocBase(), "InfoHTMLTemplate.txt");
        }
        return htmlTemplate;
    }



    public void setBg(Color c) {
        dlfp.setBg(c);
        htmlPane.setBackground(c);
        super.setBg(c);
    }



    @SuppressWarnings("unused")
    public void labelAction(String s,  boolean b) {

    }






    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }


    @SuppressWarnings("unused")
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @SuppressWarnings("unused")
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }



}
