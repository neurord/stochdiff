package org.catacomb.druid.swing;



import org.catacomb.druid.swing.dnd.DdndTransferable;
import org.catacomb.druid.swing.dnd.DragAndDrop;
import org.catacomb.report.E;

import java.awt.dnd.*;

import javax.swing.tree.TreePath;

import java.awt.Point;
import java.awt.Rectangle;

import java.awt.image.*;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.AlphaComposite;
import java.awt.Cursor;

public class DTreeDragSource implements DragSourceListener, DragSourceMotionListener, DragGestureListener {

    DragSource source;

    DragGestureRecognizer recognizer;

    DdndTransferable transferable;


    DTree sourceTree;

    BufferedImage dragImg;
    Point imgOffset;


    public DTreeDragSource(DTree tree, int actions) {
        sourceTree = tree;
        source = new DragSource();
        source.addDragSourceMotionListener(this);
        recognizer = source.createDefaultDragGestureRecognizer(sourceTree,
                     actions, this);
    }

    /*
     * Drag Gesture Handler
     */
    public void dragGestureRecognized(DragGestureEvent dge) {
        TreePath path = sourceTree.getSelectionPath();
        if ((path == null) || (path.getPathCount() <= 1)) {
            // We can't move the root node or an empty selection
            return;
        }

        String sdrag = path.getLastPathComponent().toString();



        transferable = new DdndTransferable(path);

        Point ptDragOrigin = dge.getDragOrigin();
        //  TreePath path = getPathForLocation(ptDragOrigin.x, ptDragOrigin.y);
        Rectangle raPath = sourceTree.getPathBounds(path);


        int rw = (int)(raPath.getWidth());
        int rh = (int)(raPath.getHeight());
        dragImg = new BufferedImage(rw, rh, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = dragImg.createGraphics();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 0.5f));

        g2.setColor(Color.white);
        g2.fillRect(0, 0, rw, rh);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
        g2.setColor(Color.blue);
        g2.drawString(sdrag, 14, 16);

        imgOffset = new Point(ptDragOrigin.x-raPath.x, ptDragOrigin.y-raPath.y);

        DragAndDrop.getDnD().setDragImage(dragImg, imgOffset);

        // source.startDrag(dge, DragSource.DefaultLinkDrop, dragImg, imgOffset, transferable, this);
        source.startDrag(dge, Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), dragImg, imgOffset, transferable, this);

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
        /*
         * to support move or copy, we have to check which occurred:
         */
        E.info("Drop Action: " + dsde.getDropAction());
        if (dsde.getDropSuccess()
                && (dsde.getDropAction() == DnDConstants.ACTION_MOVE)) {
            E.info("was move - should remove original");
        }

        /*
         * to support move only... if (dsde.getDropSuccess()) {
         * ((DefaultTreeModel)sourceTree.getModel()).removeNodeFromParent(oldNode); }
         */
    }

    public void dragMouseMoved(DragSourceDragEvent dsde) {
        //E.info("drag moved " + dsde);

    }
}


