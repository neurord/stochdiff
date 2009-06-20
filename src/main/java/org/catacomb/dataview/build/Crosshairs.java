

package org.catacomb.dataview.build;

import org.catacomb.graph.gui.Painter;

import java.awt.Color;


public class Crosshairs extends DisplayablePoint {



    public void instruct(Painter p, int wf) {
        if (hasData) {
            Color c = getColor();
            p.drawFixedSizeLine(xpt, ypt, c, 0, size*wf, wf);
            p.drawFixedSizeLine(xpt, ypt, c, 0, -size*wf, wf);
            p.drawFixedSizeLine(xpt, ypt, c, size*wf, 0, wf);
            p.drawFixedSizeLine(xpt, ypt, c, -size*wf, 0, wf);
        }
    }




}
