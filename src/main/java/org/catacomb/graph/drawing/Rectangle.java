package org.catacomb.graph.drawing;

import org.catacomb.graph.gui.Painter;

import java.awt.Color;



public class Rectangle extends FixedDrawingComponent {


    public double x;
    public double y;
    public double rx;
    public double ry;


    public Rectangle() {
    }



    public Rectangle(double x1, double y1, double rx1, double ry1) {
        x = x1;
        y = y1;
        rx = rx1;
        ry = ry1;
        setColor(Color.black);
        setClosed();
        setFillColor(Color.orange);
    }


    public void setSize(double sx, double sy) {
        rx = sx;
        ry = sy;
    }

    public void instruct(Painter p, double offx, double offy, double scale) {
        if (isFilled()) {
            p.drawFilledRectangle(offx + scale * x, offy + scale * y, scale * rx, scale * ry, getFillColor(), getColor(), getWidth(), widthIsPixels());
        } else {
            p.drawRectangle(offx + scale * x, offy + scale * y, scale * rx, scale * ry, getColor(), getWidth(), widthIsPixels());
        }
    }



    public void applyToShape(Shape shp) {
        shp.setCurviness(0.0);
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
