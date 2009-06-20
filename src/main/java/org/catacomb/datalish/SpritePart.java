package org.catacomb.datalish;

import org.catacomb.datalish.SColor;
import org.catacomb.datalish.array.Array;


public class SpritePart {

    double[] xpts;
    double[] ypts;
    double lineWidth;
    SColor lineColor;
    SColor fillColor;

    int ocf;

    public SpritePart(double[] xp, double[] yp, double lw, SColor lc, SColor fc, int c) {
        xpts = xp;
        ypts = yp;
        lineWidth = lw;
        lineColor = lc;
        fillColor = fc;
        ocf = c;

    }

    public void pushBox(Box b) {
        b.extendTo(xpts, ypts);

    }

    public double[] copyXpts() {
        return Array.arrayCopy(xpts);
    }

    public double[] copyYpts() {
        return Array.arrayCopy(ypts);

    }

    public boolean open() {
        return (ocf == 0);
    }

    public boolean closed() {
        return (ocf == 1);
    }

    public boolean filled() {
        return (ocf == 2);
    }

    public SColor getLineColor() {
        return lineColor;
    }

    public SColor getFillColor() {
        return fillColor;
    }

}
