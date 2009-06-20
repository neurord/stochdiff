package org.catacomb.druid.swing;


import java.awt.Color;
import java.awt.Font;

import javax.swing.JProgressBar;

import org.catacomb.interlish.interact.DComponent;


public class DProgressReport extends JProgressBar implements DComponent {
    static final long serialVersionUID = 1001;

    public final static int imax = 200;

    static Font plainfont;


    public DProgressReport() {
        super(0, imax);
        setBorderPainted(false);
        setPlainFont();
        setStringPainted(true);
    }



    public void setTooltip(String s) {
        setToolTipText(s);
    }

    public void setBg(Color c) {
        setBackground(c);

        setBorder(BorderUtil.makeBorder(BorderUtil.ETCHED_UP, c));

    }


    public void setPlainFont() {
        if (plainfont == null) {
            plainfont = new Font("sansserif", Font.PLAIN, 12);
        }

        setFont(plainfont);
    }


}
