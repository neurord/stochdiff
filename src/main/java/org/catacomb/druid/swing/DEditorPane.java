
package org.catacomb.druid.swing;

import java.awt.Color;
import java.awt.Font;
import java.io.StringReader;

import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;


public class DEditorPane extends JEditorPane {
    static final long serialVersionUID = 1001;

    static Font plainfont;

    static Font boldfont;


    HTMLEditorKit htmlEditorKit;
    StyleSheet styleSheet;




    public DEditorPane() {
        super();
        htmlEditorKit = new HTMLEditorKit();
        setEditorKit(htmlEditorKit);
        setPlainFont();

    }


    public void setBg(Color c) {
        setBackground(c);
    }


    public void setStyleSheet(StyleSheet ss) {
        styleSheet = ss;
        htmlEditorKit.setStyleSheet(ss);
    }


    public StyleSheet getStyleSheet() {
        return styleSheet;
    }


    public void showHTML(String s) {
        //      hek.createDefaultDocument();

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


    public void setPlainFont() {
        if (plainfont == null) {
            plainfont = new Font("SansSerif", Font.PLAIN, 10);
        }

        //      setFont(plainfont);
    }


    public void setBoldFont() {
        if (boldfont == null) {
            boldfont = new Font("SansSerif", Font.BOLD, 10);
        }
        //      setFont(boldfont);
    }
}
