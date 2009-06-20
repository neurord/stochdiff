
package org.catacomb.graph.arbor;


import org.catacomb.be.XYLocation;


public class ManipXYLocation implements XYLocation {

    double xpos;
    double ypos;

    public ManipXYLocation(double x, double y) {
        xpos = x;
        ypos = y;
    }


    public double getX() {
        return xpos;
    }

    public double getY() {
        return ypos;
    }

}
