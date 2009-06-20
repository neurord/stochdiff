package org.catacomb.dataview;

import java.awt.Color;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Painter;


public class StringPlotElement extends PlotElement {

    double xpt;
    double ypt;
    String txt;

    int dxo = 0;
    int dyo = 0;

    public StringPlotElement(double x, double y, String s) {
        this(x, y, Color.white, s);
    }

    public StringPlotElement(double x, double y, Color c, String s) {
        this(x, y, Color.white, s, 0, 0);
    }

    public StringPlotElement(double x, double y, Color c, String s, int xo, int yo) {
        xpt = x;
        ypt = y;
        col = c;
        txt = s;
        dxo = xo;
        dyo = yo;
    }



    public void instruct(Painter p) {
        p.setColor(col);
        if (dxo == 0 && dyo == 0) {
            p.drawText(txt, xpt, ypt);
        } else {
            p.drawLineOffsetText(txt, xpt, ypt, dxo, dyo);
        }
    }


    public void push(Box b) {
        b.push(xpt, ypt);
    }

}
