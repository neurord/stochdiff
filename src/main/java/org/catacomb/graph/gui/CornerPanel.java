package org.catacomb.graph.gui;

import org.catacomb.interlish.structure.IntActor;
import org.catacomb.report.E;


import java.awt.Dimension;
import java.awt.FlowLayout;

import java.awt.Color;

public class CornerPanel extends BasePanel implements IntActor {
    static final long serialVersionUID = 1001;

    PickWorldCanvas pickWorldCanvas;

    IconButton ibut;

    public CornerPanel(int w, int h, PickWorldCanvas pwc) {
        super();

        setPreferredSize(new Dimension(w, h));

        pickWorldCanvas = pwc;

        setLayout(new FlowLayout(FlowLayout.RIGHT));


        // ibut = new IconButton("frame", 0, this);
        //   add(ibut);

        setBg(Color.black);
    }


    public void setBg(Color c) {
        setBackground(c);
        if (ibut != null) {
            ibut.setBg(c);
        }
    }


    public void intAction(int imode) {
        E.info("time to frame...");
        pickWorldCanvas.viewAction("frame");
    }




}
