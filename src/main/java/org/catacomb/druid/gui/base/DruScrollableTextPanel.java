package org.catacomb.druid.gui.base;

import org.catacomb.druid.swing.DTextCanvas;

import java.awt.Color;

public class DruScrollableTextPanel extends DruScrollPanel {
    static final long serialVersionUID = 1001;

    DTextCanvas canvas;


    public DruScrollableTextPanel() {
        super();

        canvas = new DTextCanvas();

        subAddDComponent(canvas);

        canvas.setParentContainer(getGUIPeer());
    }

    public void setBg(Color c) {
        canvas.setBackground(c);
        super.setBg(c);
    }
    @SuppressWarnings("unused")
    public void labelAction(String s, boolean b) {

    }

    public DTextCanvas getCanvas() {
        return canvas;
    }





}
