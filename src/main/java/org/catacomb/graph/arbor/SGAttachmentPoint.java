package org.catacomb.graph.arbor;



import org.catacomb.be.Position;
import org.catacomb.be.XYLocation;
import org.catacomb.interlish.content.ConnectionFlavor;
import org.catacomb.interlish.structure.AttachmentPoint;


public class SGAttachmentPoint implements AttachmentPoint {


    private XYLocation xyloc;

    private ConnectionFlavor flavor;
    private String id;


    public SGAttachmentPoint() {
        flavor = new ConnectionFlavor("CellProbe"); // ADHOC
        id = "undefined";
    }



    public void setPosition(double x, double y) {
        xyloc = new ManipXYLocation(x, y);
    }


    public void setID(String s) {
        id = s;
    }

    public void setFlavor(ConnectionFlavor cf) {
        flavor = cf;
    }


    public XYLocation getXYLocation() {
        return xyloc;
    }

    public ConnectionFlavor getFlavor() {
        return flavor;
    }

    public String getID() {
        return id;
    }



    public void setPosition(Position pos) {
        setPosition(pos.getX(), pos.getY());
    }
}


