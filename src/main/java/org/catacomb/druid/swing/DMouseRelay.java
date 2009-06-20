package org.catacomb.druid.swing;

import org.catacomb.interlish.structure.MouseActor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class DMouseRelay implements MouseListener {

    MouseActor mouseActor;


    public DMouseRelay(MouseActor ma) {
        mouseActor = ma;
    }


    public void mousePressed(MouseEvent me) {
        mouseActor.mouseButtonPressed();
    }

    public void mouseEntered(MouseEvent me) {}
    public void mouseExited(MouseEvent me) {}
    public void mouseReleased(MouseEvent me) {}

    public void mouseClicked(MouseEvent me) {}


}
