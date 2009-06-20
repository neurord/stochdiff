package org.catacomb.interlish.content;


import java.util.ArrayList;

import org.catacomb.datalish.SColor;

public class RShape {

    public static final int OPEN = 0;
    public static final int CLOSED = 1;
    public static final int FILLED = 2;

    double[] xpts;
    double[] ypts;

    double lineWidth;
    SColor lineColor;
    SColor fillColor;

    int closure;


    public RShape(double[] x, double[] y, double lineW,
                  SColor lineC, SColor fillC, int rsc) {
        xpts = x;
        ypts = y;
        lineWidth = lineW;
        lineColor = lineC;
        fillColor = fillC;
        closure = rsc;
    }


    public double[] getXPts() {
        return xpts;
    }

    public double[] getYPts() {
        return ypts;
    }

    public double getLineWidth() {
        return lineWidth;
    }

    public SColor getLineColor() {
        return lineColor;
    }

    public SColor getFillColor() {
        return fillColor;
    }

    public int getOCF() {
        return closure; // ADHOC ocf expects 0, 1, 2 for Open, Closed, Filled;
    }

    public double meanRadius() {
        double d = 0.;
        for (int i = 0; i < xpts.length; i++) {
            d += Math.sqrt(xpts[i] * xpts[i] + ypts[i] * ypts[i]);
        }
        d /= xpts.length;
        return d;
    }


    public ArrayList<double[]> getBoundarySegments() {
        ArrayList<double[]> ret = new ArrayList<double[]>();
        for (int i = 0; i < xpts.length-1; i++) {
            double[] seg = {xpts[i], ypts[i], xpts[i+1], ypts[i+1]};
            ret.add(seg);
        }
        if (closure == CLOSED || closure == FILLED) {
            int n = xpts.length;
            double[] cseg = {xpts[n-1], ypts[n-1], xpts[0], ypts[0]};
            ret.add(cseg);
        }
        return ret;
    }


}
