package org.catacomb.graph.gui;

import org.catacomb.interlish.content.Polypoint;
import org.catacomb.interlish.content.VectorSprite;


public class SimpleVectorSprite implements VectorSprite {

    int lineColor;
    int fillColor;

    Polypoint bdry;




    public SimpleVectorSprite(double[] xp, double[] yp, int fc, int lc) {
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


    public static SimpleVectorSprite makeDefaultSprite(double f) {
        double[] xp = {-0.5, -1., 1., -1.};
        double[] yp = {0., 0.6, 0., -0.6};
        for (int i = 0; i < xp.length; i++) {
            xp[i] *= f;
            yp[i] *= f;
        }

        SimpleVectorSprite ret = new SimpleVectorSprite(xp, yp, 255, 0);
        return ret;
    }
}
