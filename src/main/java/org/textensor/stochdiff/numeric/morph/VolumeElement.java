//6 18 2007: WK added a boolean variable (submembrane), and three functions (isSubmembrane, getSubmembrane, and setSubmembrane)
//written by Robert Cannon
package org.textensor.stochdiff.numeric.morph;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.Position;


public abstract class VolumeElement {
    double cx;
    double cy;
    double cz;

    String label;
    String region;

    double volume;
    double deltaZ;
    double exposedArea;

    private int icache;

    ArrayList<ElementConnection> connections;

    Position[] boundary;

    Position[] surfaceBoundary = null;

    boolean fixcon = false;


    boolean submembrane = false; //true ifn this volume element lies on submembrane


    String groupID = null;


    public VolumeElement() {
        connections = new ArrayList<ElementConnection>();
    }


    public void setCenterPosition(double x, double y, double z) {
        cx = x;
        cy = y;
        cz = z;
    }

    public double getX() {
        return cx;
    }

    public double getY() {
        return cy;
    }

    public double getZ() {
        return cz;
    }


    public boolean isSubmembrane() {
        return submembrane;
    }

    public void setSubmembrane() {
        submembrane = true;
    }


    public void setLabel(String s) {
        label = s;
    }

    public String getLabel() {
        return label;
    }

    public void setRegion(String s) {
        region = s;
    }

    public String getRegion() {
        return region;
    }


    public ArrayList<ElementConnection> getConnections() {
        fixcon = true;
        return connections;

    }


    public void setVolume(double v) {
        volume = v;
    }

    public void setDeltaZ(double d) {
        deltaZ = d;
    }

    public void setExposedArea(double ea) {
        exposedArea = ea;
    }



    public void coupleTo(VolumeElement vx, double ca) {
        // ca is the area of contact between the elements;
        if (fixcon) {
            E.warning("adding a connection after they've already been used?");
        }
        connections.add(new ElementConnection(this, vx, ca));
    }

    public double getVolume() {
        return volume;
    }

    public double getExposedArea() {
        return exposedArea;
    }


    //<--WK
    public boolean getSubmembrane() {
        return submembrane;
    }
    //WK-->

    public void cache(int ind) {
        icache = ind;
    }

    public int getCached() {
        return icache;
    }

    public void setBoundary(Position[] pbdry) {
        boundary = pbdry;
    }

    public void setSurfaceBoundary(Position[] psb) {
        surfaceBoundary = psb;
    }

    public Position[] getSurfaceBoundary() {
        return surfaceBoundary;
    }



    public abstract String getAsText();


    public abstract String getAsPlainText();


    public abstract String getHeadings();





    public void setGroupID(String lroot) {
        groupID = lroot;

    }

    public String getGroupID() {
        return groupID;
    }

}
