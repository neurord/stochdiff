
package org.catacomb.druid.gui.edit;

import org.catacomb.druid.swing.DHTMLPane;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.structure.*;


import java.awt.Color;


public class DruHTMLPanel extends DruGCPanel
    implements TextSettable, PageDisplay, HyperlinkHandler, ValueWatcher {

    static final long serialVersionUID = 1001;

    DHTMLPane dHTMLPane;

    // PageSupplier pageSupplier;

    StringValue stringValue;

    boolean preformatted = false;

    public DruHTMLPanel() {
        super();
        setSingle();
        dHTMLPane = new DHTMLPane();
        dHTMLPane.setDefaultStyleSheet();
        addDComponent(dHTMLPane);
    }


    public void setText(String s) {
        setContent(s);
    }

    public void setPreformatted(boolean b) {
        preformatted = b;
    }

    public void setBg(Color c) {
        dHTMLPane.setBg(c);
        super.setBg(c);
    }


    public void setContent(String s) {
        dHTMLPane.showHTML(s);
    }


    public void setPageSupplier(PageSupplier ps) {
        // pageSupplier = ps;
        showPage(ps.getPage("/"));
    }


    public void setPage(Page p) {
        showPage(p);
    }


    public void showHTML(String txt) {
        dHTMLPane.showHTML(txt);
    }


    public void showPage(Page p) {
        dHTMLPane.showHTML(p.getHTMLText());
    }


    public void setLinkAction(String linkAction) {
        methodName = linkAction;
        dHTMLPane.setHyperlinkHandler(this);
    }


    public void hyperlinkClicked(String tgt) {
        action(tgt);
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
        if (preformatted) {
            s = "<pre>\n" + s + "\n</pre>\n";
        }
        dHTMLPane.showHTML(s);
    }


}
