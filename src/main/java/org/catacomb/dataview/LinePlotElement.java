package org.catacomb.dataview;

import java.awt.Color;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Painter;


public class LinePlotElement extends PlotElement {

    double[] xpts;
    double[] ypts;


    public LinePlotElement(double[] x, double[] y) {
        this(x, y, Color.white);
    }

    public LinePlotElement(double[] x, double[] y, Color c) {
        xpts = x;
        ypts = y;
        col = c;
    }

    public LinePlotElement(double[] x, double[] y, Color c, String s) {
        this(x, y, c);
        label = s;
    }

    public void instruct(Painter p) {
        p.setColor(col);
        p.drawPolyline(xpts, ypts, xpts.length);
    }


    public void push(Box b) {
        b.push(xpts, ypts);
    }

}
