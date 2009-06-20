package org.catacomb.graph.drawing;

import org.catacomb.graph.gui.Painter;



public class StraightLine extends FixedDrawingComponent {


    public double x0;
    public double y0;
    public double x1;
    public double y1;


    // color inherited
    // width inherited


    public StraightLine() {
        super();
    }

    public StraightLine(double xa, double ya, double xb, double yb) {
        x0 = xa;
        y0 = ya;
        x1 = xb;
        y1 = yb;
    }

    public void instruct(Painter p, double offx, double offy, double scale) {
        p.drawLine(offx + scale * x0, offy + scale * y0,
                   offx + scale * x1, offy + scale * y1,
                   getColor(), getWidth(), widthIsPixels());

    }


    public void applyToShape(Shape shp) {
        shp.setClosure(Shape.OPEN);
        shp.setCurviness(0.0);
        shp.setSymmetry(ShapeSymmetry.NONE);
        double[] xp = {x0, x1};
        shp.setXpts(xp);
        double[] yp = {y0, y1};
        shp.setYpts(yp);
    }

    public void scaleBy(double d) {
        x0 *= d;
        x1 *= d;
        y0 *= d;
        y1 *= d;
    }




}
