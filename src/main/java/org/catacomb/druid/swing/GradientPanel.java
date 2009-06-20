package org.catacomb.druid.swing;

import javax.swing.JPanel;

import org.catacomb.interlish.content.ColorTable;

import java.awt.Dimension;

import java.awt.Color;

import java.awt.Graphics;

public class GradientPanel extends JPanel {
    private static final long serialVersionUID = 1L;


    ColorTable colorTable;


    public GradientPanel(int w, int h) {

        setPreferredSize(new Dimension(w, h));
        setMinimumSize(new Dimension(w, h));
        setMaximumSize(new Dimension(w, h));
    }


    public void setColorTable(ColorTable ct) {
        colorTable = ct;
    }


    public void paintComponent(Graphics g) {
        int w = getWidth();
        int h = getHeight();

        if (colorTable == null) {
            g.setColor(Color.gray);
            g.fillRect(0, 0, w, h);

        } else {

            //       EFF - cache this!;

            for (int i = 0; i < w; i++) {
                double f = (1. * i) / (w - 1);
                g.setColor(colorTable.getFractionalColor(f));
                g.drawLine(i, 0, i, h);
            }


        }
    }

}
