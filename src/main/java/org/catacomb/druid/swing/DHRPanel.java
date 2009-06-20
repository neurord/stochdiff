
package org.catacomb.druid.swing;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import org.catacomb.interlish.interact.DComponent;


public class DHRPanel extends JPanel implements DComponent {
    static final long serialVersionUID = 1001;

    Dimension pref = new Dimension(100, 1);
    Dimension max = new Dimension(2000, 1);
    Dimension min = new Dimension(20, 1);

    Color color;

    public DHRPanel(int icol) {
        color = new Color(icol);
    }


    public void paintComponent(Graphics  g) {
        g.setColor(color);
        g.drawLine(0, 0, getWidth(), 0);
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
