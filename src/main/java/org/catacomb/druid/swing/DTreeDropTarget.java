package org.catacomb.druid.swing;


import org.catacomb.druid.swing.dnd.DragAndDrop;
import org.catacomb.report.E;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.dnd.*;
import java.awt.image.BufferedImage;

// import javax.swing.JTree;
// import javax.swing.tree.TreePath;


public class DTreeDropTarget implements DropTargetListener {

    DropTarget target;
    DTree targetTree;
    Rectangle dragBounds;


    public DTreeDropTarget(DTree tree) {
        targetTree = tree;
        target = new DropTarget(targetTree, this);

    }


    /*
    private Object getNodeForEvent(DropTargetDragEvent dtde) {
       Point p = dtde.getLocation();
       DropTargetContext dtc = dtde.getDropTargetContext();
       JTree tree = (JTree)dtc.getComponent();
       TreePath path = tree.getClosestPathForLocation(p.x, p.y);
       return path.getLastPathComponent();
    }
    */



    public void dragEnter(DropTargetDragEvent dtde) {

        E.info("drag entered " + dtde);

        dtde.rejectDrag();

        /*
         * TreeNode node = getNodeForEvent(dtde); if (node.isLeaf()) {
         * dtde.rejectDrag(); } else { // start by supporting move operations
         * //dtde.acceptDrag(DnDConstants.ACTION_MOVE);
         * dtde.acceptDrag(dtde.getDropAction()); }
         */
    }


    public void dragOver(DropTargetDragEvent dtde) {


        if (false) { // (DragSource.isDragImageSupported()) {

        } else {

            if (dragBounds != null) {
                targetTree.paintImmediately(dragBounds);
            } else {
                dragBounds = new Rectangle();
            }

            Point p = dtde.getLocation();

            Object obj = DragAndDrop.getDnD();

            if (obj instanceof ImageDragSource) {
                ImageDragSource ids = (ImageDragSource)obj;

                BufferedImage bim = ids.getDragImage();
                Point poff = ids.getDragImageOffset();

                if (bim == null) {
                    E.warning("no drag image?");
                } else {

                    // And remember where we are about to draw the new ghost image
                    dragBounds.setRect(p.x - poff.x, p.y - poff.y, bim.getWidth(), bim.getHeight());

                    Graphics g = targetTree.getGraphics();
                    g.drawImage(bim, (int)(dragBounds.getX()), (int)(dragBounds.getY()), null);
                }
            } else {
                E.warning("wrong type of source " + obj);
            }
        }



        // Object over = getNodeForEvent(dtde);
        dtde.rejectDrag();
    }


    /*
     * if (node.isLeaf()) { dtde.rejectDrag(); } else { // start by supporting
     * move operations //dtde.acceptDrag(DnDConstants.ACTION_MOVE);
     * dtde.acceptDrag(dtde.getDropAction()); } }
     *
     */


    public void dragExit(DropTargetEvent dte) {
        if (dragBounds != null) {
            targetTree.paintImmediately(dragBounds);
        }
    }



    public void dropActionChanged(DropTargetDragEvent dtde) {
    }


    public void drop(DropTargetDropEvent dtde) {

        /*
         *
         * Point pt = dtde.getLocation(); DropTargetContext dtc =
         * dtde.getDropTargetContext(); JTree tree = (JTree) dtc.getComponent();
         * TreePath parentpath = tree.getClosestPathForLocation(pt.x, pt.y);
         * DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentpath
         * .getLastPathComponent(); if (parent.isLeaf()) { dtde.rejectDrop();
         * return; }
         *
         * try { Transferable tr = dtde.getTransferable(); DataFlavor[] flavors =
         * tr.getTransferDataFlavors(); for (int i = 0; i < flavors.length; i++) {
         * if (tr.isDataFlavorSupported(flavors[i])) {
         * dtde.acceptDrop(dtde.getDropAction()); TreePath p = (TreePath)
         * tr.getTransferData(flavors[i]); DefaultMutableTreeNode node =
         * (DefaultMutableTreeNode) p .getLastPathComponent(); DefaultTreeModel
         * model = (DefaultTreeModel) tree.getModel(); model.insertNodeInto(node,
         * parent, 0); dtde.dropComplete(true); return; } } dtde.rejectDrop(); }
         * catch (Exception e) { e.printStackTrace(); dtde.rejectDrop(); }
         */
    }
}
