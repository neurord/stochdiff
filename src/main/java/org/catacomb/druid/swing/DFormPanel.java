package org.catacomb.druid.swing;

import javax.swing.JPanel;

import javax.swing.SpringLayout;

import javax.swing.JComponent;

import org.catacomb.interlish.interact.DComponent;

import java.awt.Color;


public class DFormPanel extends JPanel implements DComponent {
    private static final long serialVersionUID = 1L;


    public DFormPanel() {
        setLayout(new SpringLayout());

    }

    public void setTooltip(String s) {
        setToolTipText(s);
    }

    public void gridify(int rows, int cols) {
        SpringUtilities.makeCompactGrid(this, rows, cols, 3, 3, 4, 6);
        // args are rows, cols, initX, initY,  xPad, yPad
    }


    public void addDItem(DComponent obj) {
        add((JComponent)obj);
    }


    public void setBg(Color c) {
        setBackground(c);
    }

}
