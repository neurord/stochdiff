package org.catacomb.graph.drawing;

import org.catacomb.graph.gui.Painter;



public class Oval extends FixedDrawingComponent {



    public double x;
    public double y;
    public double rx;
    public double ry;

    public Oval() {
        super();
    }


    public void setSize(double sx, double sy) {
        rx = sx;
        ry = sy;
    }




    public void instruct(Painter p, double offx, double offy, double scale) {
        double w = getWidth();
        if (isFilled()) {
            p.drawFilledOval(offx + scale * x, offy + scale * y,
                             scale * rx, scale * ry, getFillColor(), getColor(),
                             w, widthIsPixels());
        } else if (w > 0.5) {
            p.drawOval(offx + scale * x, offy + scale * y,
                       scale * rx, scale * ry, getColor(), w, widthIsPixels());

        }
    }


    public void applyToShape(Shape shp) {
        shp.setCurviness(1.0);
        applySymmetryToShape(shp);

        double[] xpts = { x - rx, x - rx, x + rx, x + rx };
        double[] ypts = { y - ry, y + ry, y + ry, y - ry };
        shp.setXpts(xpts);
        shp.setYpts(ypts);
    }


    public void applySymmetryToShape(Shape shp) {
        shp.setSymmetry(ShapeSymmetry.RECTANGLE);
    }

}
