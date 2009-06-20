

package org.catacomb.dataview.build;

import org.catacomb.graph.gui.Painter;

import java.awt.Color;

public class Disc extends DisplayablePoint {


    public void instruct(Painter p, int wf) {
        if (hasData) {
            Color c = getColor();
            p.setColor(c);
            p.fillRectangle(xpt, ypt, c, size);
        }
    }




}
