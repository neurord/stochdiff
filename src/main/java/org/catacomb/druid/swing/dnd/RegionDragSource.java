package org.catacomb.druid.swing.dnd;


import org.catacomb.druid.swing.DTextCanvas;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;


public class RegionDragSource implements DragSourceListener, DragSourceMotionListener,
    DragGestureListener {

    DragSource source;
    DragGestureRecognizer recognizer;
    RegionBoard regionBoard;
    DTextCanvas sourceCanvas;



    public RegionDragSource(RegionBoard rb, int actions) {
        regionBoard = rb;
        sourceCanvas = rb.getCanvas();
        source = new DragSource();
        source.addDragSourceMotionListener(this);
        recognizer = source.createDefaultDragGestureRecognizer(sourceCanvas, actions, this);
    }



    public void dragGestureRecognized(DragGestureEvent dge) {
        Region reg = regionBoard.getHoverRegion();
        if (reg == null) {
//              E.info("drag recognized, but no region");
        } else {
            initiateDrag(reg, dge.getDragOrigin(), dge);
        }
    }



    public void initiateDrag(Region reg, Point ptDragOrigin, DragGestureEvent trigger) {
        String sdrag = reg.getText();

        DdndTransferable transferable = new DdndTransferable(null); // ERR
        Rectangle raPath = new Rectangle(reg.getX(), reg.getY(), reg.getW(), reg.getH());

        int rw = (int)(raPath.getWidth());
        int rh = (int)(raPath.getHeight());
        BufferedImage dragImg = new BufferedImage(rw, rh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dragImg.createGraphics();

        rw = g2.getFontMetrics().stringWidth(sdrag) + 8;
        dragImg = new BufferedImage(rw, rh, BufferedImage.TYPE_INT_ARGB);
        g2 = dragImg.createGraphics();


        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));

        g2.setColor(Color.white);
        g2.fillRect(0, 0, rw, rh);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
        g2.setColor(Color.blue);
        g2.drawString(sdrag, 4, 16);

        Point imgOffset = new Point(ptDragOrigin.x - raPath.x, ptDragOrigin.y - raPath.y);


        DragAndDrop.getDnD().setDragSource(reg.getRef());
        DragAndDrop.getDnD().setDragImage(dragImg, imgOffset);



        // source.startDrag(dge, DragSource.DefaultLinkDrop, dragImg,
        // imgOffset, transferable, this);
        source.startDrag(trigger, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), dragImg, imgOffset, transferable, this);

    }



    /*
     * Drag Event Handlers
     */
    public void dragEnter(DragSourceDragEvent dsde) {
    }


    public void dragExit(DragSourceEvent dse) {
    }


    public void dragOver(DragSourceDragEvent dsde) {
    }


    public void dropActionChanged(DragSourceDragEvent dsde) {
        System.out.println("Action: " + dsde.getDropAction());
        System.out.println("Target Action: " + dsde.getTargetActions());
        System.out.println("User Action: " + dsde.getUserAction());
    }



    public void dragDropEnd(DragSourceDropEvent dsde) {

        // if was move, remove source

        /*
        E.info("Drop Action: " + dsde.getDropAction());
        if (dsde.getDropSuccess() && (dsde.getDropAction() == DnDConstants.ACTION_MOVE)) {
           E.info("was move - should remove original");
        }
        */

    }


    public void dragMouseMoved(DragSourceDragEvent dsde) {
        // E.info("drag moved " + dsde);

    }
}
