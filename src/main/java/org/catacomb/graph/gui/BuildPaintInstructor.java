package org.catacomb.graph.gui;

import org.catacomb.datalish.Box;


public interface BuildPaintInstructor {


    boolean antialias();


    void instruct(Painter p, Builder b);


    Box getLimitBox(Painter p);


}
