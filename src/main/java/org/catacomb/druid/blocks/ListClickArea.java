package org.catacomb.druid.blocks;

import org.catacomb.druid.gui.base.DruListClickActor;

import java.util.ArrayList;


public class ListClickArea extends ArrayList {

    private static final long serialVersionUID = 1L;

    public int xmin;
    public int xmax;

    public String action;



    public DruListClickActor makeActor() {
        DruListClickActor dlca = new DruListClickActor(xmin, xmax, action);
        return dlca;

    }


}
