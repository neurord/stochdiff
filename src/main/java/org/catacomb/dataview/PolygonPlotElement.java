package org.catacomb.dataview;

import java.awt.Color;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Painter;


public class PolygonPlotElement extends PlotElement {

    double[] xpts;
    double[] ypts;


    public PolygonPlotElement(double[] x, double[] y) {
        this(x, y, Color.white);
    }

    public PolygonPlotElement(double[] x, double[] y, Color c) {
        xpts = x;
        ypts = y;
        col = c;
    }

    public PolygonPlotElement(double[] x, double[] y, Color c, String s) {
        this(x, y, c);
        label = s;
    }

    public void instruct(Painter p) {
        p.setColor(col);
        p.drawPolygon(xpts, ypts, xpts.length);
    }


    public void push(Box b) {
        b.push(xpts, ypts);
    }

}
