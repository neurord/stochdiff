package org.catacomb.druid.swing.dnd;


import org.catacomb.druid.swing.DDropTextField;
import org.catacomb.druid.swing.ImageDragSource;
import org.catacomb.report.E;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;

import javax.swing.JTextField;


public class TextFieldDropTarget implements DropTargetListener {

    DropTarget target;

    DDropTextField ddtField;

    JTextField jtField;

    Rectangle dragBounds;


    public TextFieldDropTarget(JTextField jtf, DDropTextField ddtf) {
        jtField = jtf;
        ddtField = ddtf;
        target = new DropTarget(jtf, this);
    }


    public void dragEnter(DropTargetDragEvent dtde) {

        dtde.acceptDrag(dtde.getDropAction());

    }

    public void dragOver(DropTargetDragEvent dtde) {

        if (false) { // (DragSource.isDragImageSupported()) {

        } else {

            if (dragBounds != null) {
                jtField.paintImmediately(dragBounds);
            } else {
                dragBounds = new Rectangle();
            }

            Point p = dtde.getLocation();

            Object obj = DragAndDrop.getDnD();

            if (obj instanceof ImageDragSource) {
                ImageDragSource ids = (ImageDragSource)obj;

                BufferedImage bim = ids.getDragImage();
                Point poff = ids.getDragImageOffset();

                int bw = bim.getWidth();
                int bh = bim.getHeight();

                if (bim == null) {
                    E.warning("no drag image?");
                } else {

                    // And remember where we are about to draw the new ghost image
                    dragBounds.setRect(p.x - poff.x, p.y - poff.y, bw, bh);

                    Graphics g = jtField.getGraphics();

                    // dragAt(g, p.x - poff.x + bw/2, p.y - poff.y + bh/2);

                    g.drawImage(bim, (int)(dragBounds.getX()), (int)(dragBounds.getY()), null);


                }
            } else {
                E.warning("wrong type of source " + obj);
            }
        }
        dtde.acceptDrag(dtde.getDropAction());

    }






    public void dragExit(DropTargetEvent dte) {
        if (dragBounds != null) {
            jtField.paintImmediately(dragBounds);
        }
    }


    public void dropActionChanged(DropTargetDragEvent dtde) {
    }


    public void drop(DropTargetDropEvent dtde) {

        Object obj = DragAndDrop.getDnD().getDragSource();
        ddtField.setDropee(obj);

    }

    /*
    DropTargetContext dtc = dtde.getDropTargetContext();

    E.info("drop at " + pt + " " + dtc);


    try {
     Transferable tr = dtde.getTransferable();
     DataFlavor[] flavors = tr.getTransferDataFlavors();
     for (int i = 0; i < flavors.length; i++) {
       if (tr.isDataFlavorSupported(flavors[i])) {
         dtde.acceptDrop(dtde.getDropAction());
         Object obj = tr.getTransferData(flavors[i]);

         E.info("dropping flavof " + flavors[i] + " " + obj);

         dtde.dropComplete(true);
         return;
       }
     }
     dtde.rejectDrop();
    } catch (Exception e) {
     e.printStackTrace();
     dtde.rejectDrop();
    }
    }
    */

}


