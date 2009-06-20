package org.catacomb.dataview.display;

import java.awt.Color;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Painter;

public class PolyLine extends Displayable {


    double[] xpts;
    double[] ypts;


    double width;


    public PolyLine(String lab, Color col, double[] xa, double[] ya, double w) {
        super(lab, col);

        xpts = xa;
        ypts = ya;
        width = w;
    }



    public void pushBox(Box b) {
        b.extendTo(xpts, ypts);
    }




    public void instruct(Painter p) {
        if (width < 0.1) {
            width = 1.;
        }
        p.drawPolyline(xpts, ypts, xpts.length, color, width, true);

    }

}
