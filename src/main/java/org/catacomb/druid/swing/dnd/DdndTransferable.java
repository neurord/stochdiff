package org.catacomb.druid.swing.dnd;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.tree.TreePath;


public class DdndTransferable implements Transferable {


    BufferedImage dragImage;
    Point dragOffset;




    public final static DataFlavor TREE_PATH_FLAVOR = new DataFlavor(TreePath.class,
            "Tree Path");

    DataFlavor flavors[] = { TREE_PATH_FLAVOR };

    TreePath path;

    public DdndTransferable(TreePath tp) {
        path = tp;
    }

    public synchronized DataFlavor[] getTransferDataFlavors() {
        return flavors;
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return (flavor.getRepresentationClass() == TreePath.class);
    }

    public synchronized Object getTransferData(DataFlavor flavor)
    throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return path;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public void setDragImage(BufferedImage dragImg, Point imgOffset) {
        dragImage = dragImg;
        dragOffset = imgOffset;

    }


    public BufferedImage getDragImage() {
        return dragImage;
    }

    public Point getDragImageOffset() {
        return dragOffset;
    }




}

