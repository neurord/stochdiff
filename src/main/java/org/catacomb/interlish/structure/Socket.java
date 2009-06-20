package org.catacomb.interlish.structure;

import org.catacomb.be.XYLocation;
import org.catacomb.interlish.content.ConnectionFlavor;



public interface Socket {


    XYLocation getXYLocation();

    XYLocation getRelativeXYLocation();

    String getID();

    ConnectionFlavor getFlavor();

    String getParentID();

    Object getParent();

    void parentMoved();

    boolean isExternal();

    boolean isInput();

    boolean isOutput();

}
