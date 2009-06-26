package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DBorderLayout;
import org.catacomb.druid.swing.DTextCanvas;

import java.awt.Color;


public class DruScratchPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    DTextCanvas canvas;


    public DruScratchPanel() {
        setBorderLayout(2, 2);


        canvas = new DTextCanvas();

        addDComponent(canvas, DBorderLayout.CENTER);

    }

    public void setBg(Color c) {
        canvas.setBackground(c);
        super.setBg(c);
    }


    public void labelAction(String s, boolean b) {

    }

    public DTextCanvas getCanvas() {
        return canvas;
    }


}
