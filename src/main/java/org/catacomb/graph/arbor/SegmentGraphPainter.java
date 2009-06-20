package org.catacomb.graph.arbor;


import org.catacomb.be.Position;
import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.BuildPaintInstructor;
import org.catacomb.graph.gui.Builder;
import org.catacomb.graph.gui.Painter;

import java.awt.Color;


// REFAC SegmentGraphVE should use this.



public class SegmentGraphPainter implements BuildPaintInstructor {

    SegmentGraph graph;

    boolean b_antialias;

    SegmentGraphPoint sgp1;
    SegmentGraphPoint sgp2;


    boolean b_live;

    public SegmentGraphPainter() {
        b_antialias = false;
        b_live = true;
    }


    public void setSegmentGraph(SegmentGraph sg) {
        graph = sg;
    }


    public void setBuildPoints(boolean b) {
        b_live = b;
    }


    public void setAntialias(boolean b) {
        b_antialias = b;
    }


    public boolean antialias() {
        return b_antialias;
    }





    public void instruct(Painter painter, Builder builder) {
        if (graph == null) {
            return;
        }

        // paint the protos first;

        for (SegmentGraphPoint sgp : graph.getPoints()) {
            paintReal(sgp, painter, builder);
        }

    }




    public void paintReal(SegmentGraphPoint sgp,
                          Painter painter, Builder builder) {

        if (b_live) {
            builder.addPoint(sgp);
        }

        SegmentGraphPoint[] nbrs = sgp.getNeighbors();
        int index = sgp.getIndex();


        painter.setColor(Color.white);
        painter.drawCircle(sgp.getPosition(), sgp.getRadius());


        for (int i = 0; i < nbrs.length; i++) {
            if (nbrs[i].getIndex() > index) {
                painter.setColor(Color.white);
                drawSegment(painter, sgp, nbrs[i]);
            }
        }
    }


    public void drawSegment(Painter painter,
                            SegmentGraphPoint sgpa, SegmentGraphPoint sgpb) {
        Position posa = sgpa.getPosition();
        double x1 = posa.getX();
        double y1 = posa.getY();
        double r1 = sgpa.getRadius();

        Position posb = sgpb.getPosition();
        double x2 = posb.getX();
        double y2 = posb.getY();
        double r2 = sgpb.getRadius();


        painter.drawCarrotSides(x1, y1, r1,  x2, y2, r2);
    }


    public Box getLimitBox() {
        // TODO Auto-generated method stub
        return null;
    }


    public Box getLimitBox(Painter p) {
        // TODO Auto-generated method stub
        return null;
    }







}
