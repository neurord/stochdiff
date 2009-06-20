package org.catacomb.graph.drawing;


import java.awt.Color;



public class Circle extends Oval {


    public Circle() {
        super();
    }

    public static Circle defaultCircle() {
        Circle d = new Circle();
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
