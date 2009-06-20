package org.catacomb.druid.swing.dnd;


import org.catacomb.druid.swing.DTextCanvas;
import org.catacomb.druid.swing.ImageDragSource;
import org.catacomb.report.E;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;




public class RegionDropTarget implements DropTargetListener {

    DropTarget target;

    RegionBoard regionBoard;

    DTextCanvas textCanvas;

    Rectangle dragBounds;


    Region previousDragRegion;


    public RegionDropTarget(RegionBoard rb) {
        regionBoard = rb;
        textCanvas = rb.getCanvas();
        target = new DropTarget(textCanvas, this);
    }


    public void dragEnter(DropTargetDragEvent dtde) {

        dtde.acceptDrag(dtde.getDropAction());

        /*
        TreeNode node = getNodeForEvent(dtde);
        if (node.isLeaf()) {
         dtde.rejectDrag();
        } else {
         // start by supporting move operations
         //dtde.acceptDrag(DnDConstants.ACTION_MOVE);
         dtde.acceptDrag(dtde.getDropAction());
        }
        */
    }

    public void dragOver(DropTargetDragEvent dtde) {

        if (false) { // (DragSource.isDragImageSupported()) {

        } else {

            if (dragBounds != null) {
                textCanvas.paintImmediately(dragBounds);
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

                    Graphics g = textCanvas.getGraphics();

                    dragAt(g, p.x - poff.x + bw/2, p.y - poff.y + bh/2);

                    g.drawImage(bim, (int)(dragBounds.getX()), (int)(dragBounds.getY()), null);


                }
            } else {
                E.warning("wrong type of source " + obj);
            }
        }
        dtde.acceptDrag(dtde.getDropAction());

    }




    public void dragAt(Graphics g, int i, int j) {
        clearDragEcho();

        Region reg = regionBoard.getDragOverRegion(i, j);
        if (reg != null) {
            if (reg.acceptsDrops()) {
                g.setColor(Color.green);
                g.drawRect(reg.getX(), reg.getY(), reg.getW(), reg.getH());
                g.drawRect(reg.getX()+1, reg.getY()+1, reg.getW()-2, reg.getH()-2);
            }
            previousDragRegion = reg;
        }
    }


    public void clearDragEcho() {
        if (previousDragRegion != null) {
            regionBoard.unecho(previousDragRegion);
            previousDragRegion = null;
        }
    }




    public void dragExit(DropTargetEvent dte) {
        if (dragBounds != null) {
            textCanvas.paintImmediately(dragBounds);
        }
    }


    public void dropActionChanged(DropTargetDragEvent dtde) {
    }


    public void drop(DropTargetDropEvent dtde) {


        //   Point pt = dtde.getLocation();
        //   DropTargetContext dtc = dtde.getDropTargetContext();

        Region dropReg = regionBoard.getDragOverRegion();
        Object dropee = DragAndDrop.getDnD().getDragSource();

        if (dropReg == null) {
            regionBoard.dropGeneral(dropee);

        } else if (dropReg.acceptsDrops()) {
            regionBoard.dropOn(dropee, dropReg);
        }

    }


}

