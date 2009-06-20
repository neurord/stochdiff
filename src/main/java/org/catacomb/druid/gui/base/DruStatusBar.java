package org.catacomb.druid.gui.base;

import java.awt.Color;

import org.catacomb.druid.swing.DLabel;
import org.catacomb.interlish.structure.Receiver;
import org.catacomb.interlish.structure.StatusDisplay;

public class DruStatusBar extends DruPanel implements Receiver, StatusDisplay {
    static final long serialVersionUID = 1001;

    DLabel label;



    public DruStatusBar() {
        label = new DLabel(" messages ");

        addSingleDComponent(label);
    }



    public void showStatus(String txt) {
        // wrap markup in <html> if not there to tell label to render as html;
        if (txt.indexOf("/>") > 0 || txt.indexOf("</") > 0) {
            if (txt.startsWith("<html>")) {
                label.setText(txt);
            } else {
                label.setText("<html>" + txt + "</html>");
            }

        } else {
            label.setText(txt);
        }

    }


    public void setBg(Color c) {
        super.setBg(c);
        label.setBg(c);
    }



}
