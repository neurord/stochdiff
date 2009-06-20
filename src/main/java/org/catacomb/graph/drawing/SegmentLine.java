package org.catacomb.graph.drawing;

import org.catacomb.graph.gui.Painter;
import org.catacomb.util.ArrayUtil;


import java.awt.Color;
import java.util.StringTokenizer;



public class SegmentLine extends FixedDrawingComponent {


    // color inherited
    // width inherited


    // these are alternatives - either use xyxy or the two arrays
    // xyxy is handy for creating models by hand, but the two arrays can bse
    // serialized by default;
    public String xyxy;

    public double[] xpts;
    public double[] ypts;


    private double[] p_x;
    private double[] p_y;


    private double[] p_wkx;
    private double[] p_wky;



    public SegmentLine() {

    }



    public SegmentLine(double[] ax, double[] ay, Color c) {
        p_x = ax;
        p_y = ay;
        setWidth(1.0);
        setColor(c);
        init();
    }



    public double[] getXPts() {
        return p_x;
    }


    public double[] getYPts() {
        return p_y;
    }



    public void instruct(Painter p, double offx, double offy, double scale) {


        int n = p_x.length;
        for (int i = 0; i < n; i++) {
            p_wkx[i] = offx + scale * p_x[i];
            p_wky[i] = offy + scale * p_y[i];
        }

        p.drawPolyline(p_wkx, p_wky, n, getColor(), getWidth(), widthIsPixels());
    }



    public void reReference() {
        super.reReference();

        if (xyxy != null) {
            StringTokenizer st = new StringTokenizer(xyxy, ", \n");
            int nt = st.countTokens();
            int hn = nt / 2;
            p_x = new double[hn];
            p_y = new double[hn];
            for (int i = 0; i < hn; i++) {
                p_x[i] = (new Double(st.nextToken())).doubleValue();
                p_y[i] = (new Double(st.nextToken())).doubleValue();
            }
        } else if (xpts != null) {
            p_x = xpts;
            p_y = ypts;
            // check for same length! MISSING
        }

        init();
    }


    private void init() {
        if (p_x == null) {
            p_x = new double[0];
            p_y = new double[0];
        }


        int n = p_x.length;
        p_wkx = new double[n];
        p_wky = new double[n];
    }



    public void shift(double dx, double dy) {
        for (int i = 0; i < p_x.length; i++) {
            p_x[i] += dx;
            p_y[i] += dy;
        }
    }


    public void scaleBy(double fac) {
        for (int i = 0; i < p_x.length; i++) {
            p_x[i] *= fac;
            p_y[i] *= fac;
        }
    }



    public void applyToShape(Shape shp) {
        shp.setClosure(Shape.OPEN);
        shp.setCurviness(0.0);
        shp.setSymmetry(ShapeSymmetry.NONE);
        shp.setXpts(ArrayUtil.copyDArray(getXPts()));
        shp.setYpts(ArrayUtil.copyDArray(getYPts()));
    }


    public static SegmentLine unitBox() {
        double[] xp = { -1., 1., 1., -1., -1. };
        double[] yp = { 1., 1., -1., -1., 1. };

        SegmentLine ret = new SegmentLine(xp, yp, Color.gray);
        return ret;
    }


}
