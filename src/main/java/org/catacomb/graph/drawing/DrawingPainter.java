package org.catacomb.graph.drawing;


import org.catacomb.be.Position;
import org.catacomb.graph.gui.Painter;
import org.catacomb.report.E;


public class DrawingPainter {


    public DrawingPainter() {

    }



    public void instruct(Painter p, FixedDrawing dr, Position pos, double scale) {
        instruct(p, dr, pos.getX(), pos.getY(), scale);
    }



    public void instruct(Painter p, FixedDrawing dr, double cx, double cy, double scale) {
        if (dr == null) {
            E.warning("null drawing in paineter.instruct");

        } else {
            dr.instruct(p, cx, cy, scale);
        }
    }





}
