package org.catacomb.graph.drawing;

import org.catacomb.graph.gui.Painter;

import java.awt.Color;



public class Triangle extends FixedDrawingComponent {


    // color inherited
    // width inherited (border width)


    public double x0;
    public double y0;
    public double x1;
    public double y1;
    public double x2;
    public double y2;


    public Triangle() {
    }


    public Triangle(double ax0, double ay0,
                    double ax1, double ay1,
                    double ax2, double ay2) {
        x0 = ax0;
        y0 = ay0;
        x1 = ax1;
        y1 = ay1;
        x2 = ax2;
        y2 = ay2;

        setFillColor(Color.orange);
    }





    public void instruct(Painter p, double offx, double offy, double scale) {
        p.drawFilledTriangle(offx + scale * x0,  offy + scale * y0,
                             offx + scale * x1,  offy + scale * y1,
                             offx + scale * x2,  offy + scale * y2,
                             getFillColor(), getColor(), getWidth(), widthIsPixels());

    }


    public void applyToShape(Shape shp) {
        double[] xpts = {x0, x1, x2};
        double[] ypts = {y0, y1, y2};
        shp.setXpts(xpts);
        shp.setYpts(ypts);
    }



}
