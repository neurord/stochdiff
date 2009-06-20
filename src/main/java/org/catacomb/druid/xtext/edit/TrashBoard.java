package org.catacomb.druid.xtext.edit;

import java.awt.Graphics2D;

import org.catacomb.druid.swing.DTextCanvas;
import org.catacomb.druid.swing.dnd.Region;
import org.catacomb.druid.swing.dnd.RegionBoard;
import org.catacomb.druid.xtext.data.XType;
import org.catacomb.druid.xtext.data.XTypeStore;
import org.catacomb.interlish.content.IntPosition;
import org.catacomb.interlish.structure.TextPainter;
import org.catacomb.report.E;


public class TrashBoard extends RegionBoard
    implements TextPainter {

    XTypeStore typeStore;



    public TrashBoard(DTextCanvas c) {
        super(c);

        typeStore = XTypeStore.instance();
        canvas.setTextPainter(this);
    }


    public IntPosition getScreenPosition() {
        return canvas.getScreenPosition();
    }




    public void repaint() {
        canvas.repaint();
    }


    public void paintText(Graphics2D g) {
        clearRegions();

    }


    public void regionClicked(Region reg) {

    }


    public void dropOn(Object src, Region reg) {

    }

    public void emptyDrop(Object src) {
        dropGeneral(src);
    }

    public void dropGeneral(Object src) {
        E.info("trash drop " + src);
        if (src instanceof XType) {
            typeStore.remove((XType)src);
        }
        repaint();
    }




}
