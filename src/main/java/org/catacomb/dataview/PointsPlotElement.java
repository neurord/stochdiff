package org.catacomb.dataview;

import java.awt.Color;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Painter;


public class PointsPlotElement extends PlotElement {

    double[] xpts;
    double[] ypts;
    int nx;
    int ny;


    public PointsPlotElement(double[] x, double[] y) {
        this(x, y, Color.white);
    }

    public PointsPlotElement(double[] x, double[] y, Color c) {
        this(x, y, c, 3);
    }


    public PointsPlotElement(double[] x, double[] y, Color c, int np) {
        this(x, y, c, np, np);
    }

    public PointsPlotElement(double[] x, double[] y, Color c, int inx, int iny) {
        xpts = x;
        ypts = y;
        col = c;
        nx = inx;
        ny = iny;
    }


    // TODO REFAC  npix should always be width (but isn't...)
    public void instruct(Painter p) {
        p.setColor(col);
        if (nx >= 3) {
            for (int i = 0; i < xpts.length; i++) {
                p.drawCenteredOval(xpts[i], ypts[i], nx, ny);
            }

        } else {
            for (int i = 0; i < xpts.length; i++) {
                p.drawFilledRectangle(xpts[i], ypts[i], nx, ny, col);
            }
        }
    }



    public void push(Box b) {
        b.push(xpts, ypts);

    }

}
