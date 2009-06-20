package org.catacomb.druid.swing;

import java.awt.Point;
import java.awt.image.BufferedImage;


public interface ImageDragSource {

    public BufferedImage getDragImage();


    public Point getDragImageOffset();



}
