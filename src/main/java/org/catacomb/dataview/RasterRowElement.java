package org.catacomb.dataview;

import java.awt.Color;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Painter;


public class RasterRowElement extends PlotElement {

    double[] xpts;
    double[] vals;
    double vmin;
    double vmax;
    double y0;
    double thresh;

    public RasterRowElement(double[] x, double[] v, double a, double b, double y, double th) {
        vmin  = a;
        vmax = b;
        y0 = y;
        xpts = x;
        vals = v;
        thresh = th;
    }


    public void instruct(Painter p) {
        Color[] cols = new Color[256];
        for (int i = 0; i < 256; i++) {
            cols[i] = new Color(i, i, i);
        }

        for (int i = 0; i < xpts.length - 2; i++) {
            double v = vals[i];
            double fv = (v - vmin) / (vmax - vmin);
            if (fv < 0.) {
                fv = 0.;
            }
            if (fv > 1.) {
                fv = 1.;
            }
            p.fillRectangle(xpts[i], y0, xpts[i+1], y0+0.8, cols[(int)(255 * fv)]);
        }

        p.setColor(Color.yellow);
        for (int i = 0; i < xpts.length-2; i++) {
            if (vals[i] < thresh && vals[i+1] >= thresh) {
                p.drawLine(xpts[i], y0, xpts[i], y0+0.8);
            }
        }
    }


    public void push(Box b) {
        b.pushX(xpts);
        b.pushY(y0+1);
        b.pushY(y0);
    }

}
