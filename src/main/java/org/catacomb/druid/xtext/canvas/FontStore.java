package org.catacomb.druid.xtext.canvas;

import org.catacomb.report.E;

import java.util.HashMap;

import java.awt.Font;
import java.awt.Graphics;


public class FontStore {

    HashMap<String, Font> fontHM;

    Font activeFont;
    Font defaultFont;
    Font defaultItalicFont;

    Font h2Font;
    Font boldFont;

    static FontStore p_instance;




    private FontStore() {
        fontHM = new HashMap<String, Font>();
        activeFont = new Font("sanserif", Font.PLAIN, 12);
        h2Font = new Font("sanserif", Font.BOLD, 14);
        boldFont = new Font("sanserif", Font.BOLD, 12);
    }

    // any reason not to be a singleton?
    public static FontStore instance() {
        if (p_instance == null) {
            p_instance = new FontStore();
        }
        return p_instance;
    }


    public Font getFont(String fam, String sty, String siz) {
        String key = fam + "-" + sty + "-" + siz;

        int isiz = 12;
        if (siz == null) {
            E.warning("null size in get font ");
        } else {

            isiz = Integer.parseInt(siz);
        }

        int isty = Font.PLAIN;
        if (sty == null) {
            E.warning("null style ");
        } else if (sty.equals("plain")) {

        } else if (sty.equals("bold")) {
            isty = Font.BOLD;

        } else if (sty.equals("italic")) {
            isty = Font.ITALIC;

        } else {
            E.error("unrecognized style " + sty);
        }


        return getFont(fam, isty, isiz, key);
    }


    public Font getFont(String famin, int isty, int isiz, String key) {
        String fam = famin;
        Font ret = null;
        if (fontHM.containsKey(key)) {
            ret = fontHM.get(key);
        } else {


            if (fam == null) {
                E.warning("null family in get font ");
                fam = "serif";
            }

            ret = new Font(fam, isty, isiz);
            if (key != null) {
                fontHM.put(key, ret);
            }
        }
        return ret;
    }


    public int stringWidth(Graphics g, String text) {
        return g.getFontMetrics().stringWidth(text);
    }


    public Font getActiveFont() {
        return activeFont;
    }

    public Font getDefaultFont() {
        if (defaultFont == null) {
            defaultFont = getFont("sansserif", Font.PLAIN, 12, null);
        }
        return defaultFont;
    }


    public Font getDefaultItalicFont() {
        if (defaultItalicFont == null) {
            defaultItalicFont = getFont("sansserif", Font.ITALIC, 12, null);
        }
        return defaultItalicFont;
    }


    public Font getH2Font() {
        return h2Font;
    }

    public Font getBoldFont() {
        return boldFont;
    }

}
