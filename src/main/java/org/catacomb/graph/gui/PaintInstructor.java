package org.catacomb.graph.gui;

import org.catacomb.datalish.Box;


public interface PaintInstructor {


    boolean antialias();

    void instruct(Painter p);

    Box getLimitBox();


}
