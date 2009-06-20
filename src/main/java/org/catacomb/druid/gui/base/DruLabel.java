package org.catacomb.druid.gui.base;


import org.catacomb.druid.swing.DLabel;
import org.catacomb.report.E;

import java.awt.Color;


public class DruLabel {

    static final long serialVersionUID = 1001;

    String text;

    DLabel dLabel;


    public DruLabel(String lab) {
        this(lab, "left");
    }

    public DruLabel(String lab, String align) {
        text = lab;
        if (align != null && align.equals("right")) {
            dLabel = new DLabel(text, DLabel.TRAILING);

        } else {
            dLabel = new DLabel(text);
        }
    }


    public DLabel getGUIPeer() {
        return dLabel;
    }

    public void setText(String s) {
        dLabel.setText(s);
    }

    public void setFontBold() {
        dLabel.setFontBold();
    }

    public void setBg(Color c) {
        E.info("label setting bg " + c);
        dLabel.setBg(c);
    }
    public void setFg(Color c) {
        dLabel.setFg(c);
    }


}
