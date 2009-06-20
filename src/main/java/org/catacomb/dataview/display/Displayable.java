package org.catacomb.dataview.display;

import java.awt.Color;

import org.catacomb.datalish.Box;
import org.catacomb.graph.gui.Painter;


public abstract class Displayable {

    public String label;
    public Color color;

    public Displayable(String lbl, Color c) {
        label = lbl;
        color = c;
    }

    public abstract void pushBox(Box b);

    public abstract void instruct(Painter p);


}
