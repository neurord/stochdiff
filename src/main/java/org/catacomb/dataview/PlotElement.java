package org.catacomb.dataview;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Painter;
import java.awt.Color;

public abstract class PlotElement {

    public String label;
    public Color col = Color.white;

    public abstract void instruct(Painter p);

    public abstract void push(Box b);


    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return col;
    }


}
