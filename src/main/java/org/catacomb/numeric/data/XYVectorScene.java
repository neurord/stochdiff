package org.catacomb.numeric.data;

import org.catacomb.interlish.content.VectorScene;


public class XYVectorScene implements VectorScene {

    int color;
    double[] xpts;
    double[] ypts;



    public XYVectorScene(double[] xp, double[] yp, int col) {
        xpts = xp;
        ypts = yp;
        color = col;
    }


    public double[] getXPts() {
        return xpts;
    }

    public double[] getYPts() {
        return ypts;
    }

    public int getColor() {
        return color;
    }

}
