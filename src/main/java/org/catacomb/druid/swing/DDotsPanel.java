package org.catacomb.druid.swing;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import org.catacomb.interlish.interact.DComponent;


public class DDotsPanel extends JPanel implements DComponent {

    static final long serialVersionUID = 1001;


    Dimension pref = new Dimension(100, 1);
    Dimension max = new Dimension(2000, 1);
    Dimension min = new Dimension(20, 1);

    Color color;


    public DDotsPanel() {
        color = Color.black;
    }

    public void setBg(Color c) {
        color = c.darker();
    }

    public void paintComponent(Graphics g) {
        g.setColor(color);
        int w = getWidth();

        for (int i = 10; i < w - 10; i += 15) {
            g.drawLine(i, 2, i + 5, 2);
        }
    }


    public void setTooltip(String s) {
        setToolTipText(s);
    }


    public Dimension getPreferredSize() {
        return pref;
    }


    public Dimension getMaximumSize() {
        return max;
    }


    public Dimension getMinimumSize() {
        return min;
    }


}
