package org.catacomb.graph.drawing;

import org.catacomb.be.Position;
import org.catacomb.graph.gui.Painter;
import org.catacomb.interlish.structure.AddableTo;
import org.catacomb.report.E;


import java.awt.Color;
import java.util.ArrayList;


public class FixedDrawing implements AddableTo {

    public ArrayList<FixedDrawingComponent> items;


    public FixedDrawing() {
        items = new ArrayList<FixedDrawingComponent>();
    }


    public void instruct(Painter p, double cx, double cy, double scale) {

        for (FixedDrawingComponent fdCpt : items) {
            fdCpt.instruct(p, cx, cy, scale);
        }
    }

    public void randomInit() {
        items.add(new Square(0., 0., 1., 1.));
    }


    public ArrayList<Shape> makeShapes() {
        ArrayList<Shape> shapes = new ArrayList<Shape>();
        for (FixedDrawingComponent cpt : items) {
            if (cpt.isWrapper()) {
                // don't want it POSERR
                // certainly don't want it exploded into shapes in the primary cpt
                // maybe cases where we do?

            } else {
                Shape shp = cpt.makeShape();
                if (shp != null) {
                    shapes.add(shp);
                }
            }
        }
        return shapes;
    }


    public void addFromScaledShape(Shape sh, Position cpos, double scl) {
        items.add(new GenericShape(sh, cpos, scl));
    }



    public ArrayList<Shape> makeOffsetShapes(double offx, double offy) {
        ArrayList<Shape> shapes = makeShapes();
        for (Shape shp : shapes) {
            shp.shiftExpand(offx, offy, 1.0);

        }
        return shapes;
    }



    public void add(Object obj) {
        if (obj instanceof FixedDrawingComponent) {

            items.add((FixedDrawingComponent)obj);
        } else {
            E.error("wrong type in drawing " + obj);
        }
    }


    public ArrayList<FixedDrawingComponent> getComponents() {
        return items;
    }


    public void addStraightLine(double xa, double ya, double xb, double yb) {
        StraightLine sl = new StraightLine(xa, ya, xb, yb);
        items.add(sl);
    }

    public SegmentLine getBoundary() {

        double[] xp = { -1., 1., 1., -1., -1. };
        double[] yp = { 1., 1., -1., -1., 1. };

        SegmentLine ret = new SegmentLine(xp, yp, Color.gray);
        return ret;
    }



}
