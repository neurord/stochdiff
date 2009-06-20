package org.catacomb.graph.drawing;


import java.awt.Color;



public class Square extends Rectangle {


    public Square() {
        super();
    }

    public Square(double d, double e, double f, double g) {
        super(d, e, f, g);
    }


    public static Square defaultSquare() {
        Square d = new Square();
        d.rx = 1.0;
        d.ry = 1.0;
        d.setColor(Color.red);
        d.setFilled();
        d.setFillColor(Color.blue);
        return d;
    }



    public void applySymmetryToShape(Shape shp) {
        shp.setSymmetry(ShapeSymmetry.SQUARE);
    }

}
