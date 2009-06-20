package org.catacomb.numeric.data;

import org.catacomb.interlish.content.Polypoint;
import org.catacomb.interlish.content.VectorSprite;


public class XYVectorSprite implements VectorSprite {

    int lineColor;
    int fillColor;

    Polypoint bdry;




    public XYVectorSprite(double[] xp, double[] yp, int fc, int lc) {
        bdry = new Polypoint(xp, yp, Polypoint.FILLED);
        fillColor = fc;
        lineColor = lc;
    }



    public int getFillColor() {
        return fillColor;
    }

    public int getLineColor() {
        return lineColor;
    }

    public Polypoint getBoundary() {
        return bdry;
    }


    public static XYVectorSprite makeDefaultSprite() {
        double[] xp = {-0.5, -1., 1., -1.};
        double[] yp = {0., 0.6, 0., -0.6};

        XYVectorSprite ret = new XYVectorSprite(xp, yp, 255, 0);
        return ret;
    }
}
