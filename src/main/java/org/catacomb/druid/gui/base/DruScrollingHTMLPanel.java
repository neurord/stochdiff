
package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DHTMLPane;
import org.catacomb.druid.swing.DScrollPane;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.*;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;


public class DruScrollingHTMLPanel extends DruPanel
    implements TextSettable, PageDisplay, ValueWatcher {
    static final long serialVersionUID = 1001;

    DHTMLPane dHTMLPane;

    PageSupplier pageSupplier;

    boolean preformat = false;

    StringValue stringValue;


    public DruScrollingHTMLPanel() {
        super();
        setSingle();
        DScrollPane dsp =  new DScrollPane();
        dsp.setVerticalScrollBarAlways();
        addDComponent(dsp);

        dHTMLPane = new DHTMLPane();
        dHTMLPane.setDefaultStyleSheet();

        dsp.setViewportView(dHTMLPane);
    }


    public void setPreformat(boolean b) {
        preformat = b;
    }


    public void setStylesheetPath(String s) {
        dHTMLPane.setStylesheetPath(s);
    }

    public void setText(String sin) {
        String s = sin;
        if (preformat) {
            s = "<pre>\n" + s + "\n</pre>\n";
        }

        setContent(s);
    }


    public void setBg(Color c) {
        dHTMLPane.setBg(c);
        super.setBg(c);
    }


    public void setContent(String s) {
        dHTMLPane.showHTML(s);
    }


    public void setPageSupplier(PageSupplier ps) {
        pageSupplier = ps;
        showPage(ps.getPage("/"));
    }


    public void showPage(Page p) {
        dHTMLPane.showHTML(p.getHTMLText());
    }

    public void setStringValue(StringValue sv) {
        if (stringValue != null) {
            stringValue.removeValueWatcher(this);
        }
        stringValue = sv;
        if (stringValue == null) {
            dHTMLPane.showHTML("");
        } else {
            exportStringValueContent();

            stringValue.addValueWatcher(this);
        }
    }


    public void valueChangedBy(Value pv, Object src) {
        exportStringValueContent();
    }


    private void exportStringValueContent() {
        String s = "";
        if (stringValue != null) {
            s = stringValue.getString();
        }
        setText(s);
    }


    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }
    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }
    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return 10;
    }

}
