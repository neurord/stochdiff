package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DTextCanvas;

import java.awt.Color;

public class DruTextPanel extends DruPanel {
    static final long serialVersionUID = 1001;

    DTextCanvas canvas;


    public DruTextPanel() {
        super();
        setSingle();
        canvas = new DTextCanvas();

        addDComponent(canvas);
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
