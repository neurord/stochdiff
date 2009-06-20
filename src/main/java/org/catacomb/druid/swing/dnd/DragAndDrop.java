package org.catacomb.druid.swing.dnd;

import org.catacomb.druid.swing.ImageDragSource;

import java.awt.Point;
import java.awt.image.BufferedImage;


public class DragAndDrop implements ImageDragSource {

    BufferedImage dragImage;
    Point dragOffset;

    Object dragSource;


    static DragAndDrop instance;



    public static DragAndDrop getDnD() {
        if (instance == null) {
            instance = new DragAndDrop();
        }
        return instance;
    }



    public void setDragSource(Object obj) {
        dragSource = obj;
    }

    public  Object getDragSource() {
        return dragSource;
    }

    public void setDragImage(BufferedImage bim, Point po) {
        dragImage = bim;
        dragOffset = po;
    }

    public BufferedImage getDragImage() {
        return dragImage;
    }

    public Point getDragImageOffset() {
        return dragOffset;
    }


}
