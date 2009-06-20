package org.catacomb.druid.swing;

import org.catacomb.interlish.interact.DComponent;
import org.catacomb.interlish.structure.HyperlinkHandler;
import org.catacomb.interlish.structure.Page;
import org.catacomb.interlish.util.JUtil;
import org.catacomb.report.E;
import org.catacomb.xdoc.XdocBase;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Dimension;
import java.io.StringReader;
import java.net.URL;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;


public class DHTMLPane extends JEditorPane implements DComponent, HyperlinkListener {
    static final long serialVersionUID = 1001;

    HTMLEditorKit htmlEditorKit;
    StyleSheet styleSheet;

    static StyleSheet defaultStyleSheet;


    // String hexBgColor;


    HyperlinkHandler hyperlinkHandler;


    public DHTMLPane() {
        super();
        //  hexBgColor="#f0f0f0";

        //      setContentType("text/html");
        setEditable(false);

        htmlEditorKit = new HTMLEditorKit();
        setEditorKit(htmlEditorKit);
    }

    public void setPrefSize(int w, int h) {
        setPreferredSize(new Dimension(w, h));
    }


    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        super.paintComponent(g2);
    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }


    public void setBg(Color c) {
        setBackground(c);

        // hexBgColor = ColorUtil.hexString(c);

        colorizeAndApplyStylesheet();

    }


    public void setDefaultStyleSheet() {
        if (defaultStyleSheet == null) {
            defaultStyleSheet = loadRelStyleSheet(new XdocBase(), "defaultCSS.txt");
        }
        setStyleSheet(defaultStyleSheet);
    }


    public void setStylesheetPath(String s) {
        String[] sa = s.split(":");

        String scss = JUtil.getFileResource(sa[0], sa[1]);
        StyleSheet styles = new StyleSheet();
        try {
            styles.loadRules(new StringReader(scss), null);
        } catch (Exception ex) {
            E.error("" + ex);
        }
        setStyleSheet(styles);

    }


    public void setRelStyleSheet(Object rel, String fnm) {
        StyleSheet ss = loadRelStyleSheet(rel, fnm);
        setStyleSheet(ss);
    }





    public void clearHyperlinkListeners() {
        for (HyperlinkListener hl : getHyperlinkListeners()) {
            removeHyperlinkListener(hl);
        }
    }

    /*
    int i = 0; i < hla.length; i++) {
     removeHyperlinkListener(hla[i]);
       }
    }
    */


    public void setHyperlinkHandler(HyperlinkHandler lh) {
        hyperlinkHandler = lh;

        addHyperlinkListener(this);

    }



    private StyleSheet loadRelStyleSheet(Object ref, String fnm) {
        // cache in static hash map?? EFF
        String scss = JUtil.getRelativeResource(ref, fnm);
        StyleSheet styles = new StyleSheet();
        try {
            styles.loadRules(new StringReader(scss), null);
        } catch (Exception ex) {
            E.error("" + ex);
        }
        return styles;
    }


    public void setStyleSheet(StyleSheet ss) {
        styleSheet = ss;
        colorizeAndApplyStylesheet();
    }


    public void colorizeAndApplyStylesheet() {
        if (styleSheet != null) {

            /* NB this doesnt work - attribute color is null for some reason;
            StyleContext.NamedStyle style = (StyleContext.NamedStyle)(styleSheet.getStyle("body"));
            // styleSheet.removeStyle("body");
            //  style.removeAttribute("background-color");

             Enumeration<?> atts = style.getAttributeNames();
             while (atts.hasMoreElements()) {
                E.info("att name " + atts.nextElement());
             }
            E.info(style.toString() + " " + style.getAttribute("color"));
            //style.addAttribute("background-color", java.awt.Color.red); // hexBgColor);
             // styleSheet.addStyle("body", style);
              */

            htmlEditorKit.setStyleSheet(styleSheet);
        }
    }

    public void showPage(Page page) {
        showHTML(page.getHTMLText());
    }



    public void showHTML(String s) {
        HTMLDocument doc = new HTMLDocument(styleSheet);
        try {
            htmlEditorKit.read(new StringReader(s), doc, 0);
        } catch (Exception ex) {

        }

        setDocument(doc);

        /*
        StyleSheet styles = doc.getStyleSheet();
        E.info("doc ss " + styles.hashCode() + "   src ss " + styleSheet.hashCode());

        E.info("doc ek " + htmlEditorKit.hashCode() + "   src ek " + getEditorKit().hashCode());
        */
    }



    public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            String s = e.getDescription();
            URL u = e.getURL();
            if (u != null) {
                hyperlinkHandler.hyperlinkClicked(u.toString());
            } else {
                hyperlinkHandler.hyperlinkClicked(s);
            }

        } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {

        } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {

        }
    }


}

