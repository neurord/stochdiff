package org.catacomb.druid.swing;


// for the laf
import java.awt.Color;
import java.awt.Graphics;

import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicSplitPaneDivider;


public class DSplitPaneDivider extends BasicSplitPaneDivider {

    static final long serialVersionUID = 1001;

    Color cbg;
    Color cbgd;

    DSplitPaneUI dspui;


    DSplitPaneDivider(DSplitPaneUI ui) {
        super(ui);
        dspui = ui;
        setBorder(new EmptyBorder(0, 0, 0, 0));
    }


    public void paint(Graphics g) {

        Color c = dspui.getBackground();

        if (cbg == null || !(cbg.equals(c))) {
            cbg = c;
            cbgd = new Color(cbg.getRed() - 24, cbg.getGreen() - 24, cbg.getBlue() - 24);
        }


        int w = getWidth();
        int h = getHeight();


        g.setColor(cbg);
        g.fillRect(0, 0, w, h);

        g.setColor(cbgd);

        /*
        if (w > h) {
           for (int i = 10; i < w - 10; i += 25) {
             // g.drawLine(i, 2, i + 5, 2);
              g.fillRect(i, 1, 3, 3);

           }

        } else {
           for (int i = 10; i < h - 10; i += 15) {
              g.drawLine(2, i, 2, i + 5);
           }
        }
        */
    }


}
