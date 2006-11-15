package org.textensor.stochdiff.numeric.morph;

import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.Position;


public class VolumeElement {

    String label;
    String region;

    double cx;
    double cy;
    double cz;
    double volume;
    double exposedArea;

    private int icache;

    ArrayList<ElementConnection> connections;

    Position[] boundary;

    Position[] surfaceBoundary = null;

    boolean fixcon = false;


    // these are for caching connection areas to neighbors for
    // cuboid volumes only
    double alongArea;
    double sideArea;
    double topArea;




    public VolumeElement() {
        connections = new ArrayList<ElementConnection>();
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


    public void setCenterPosition(double x, double y, double z) {
        cx = x;
        cy = y;
        cz = z;
    }

    public void setVolume(double v) {
        volume = v;
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


    public double getX() {
        return cx;
    }

    public double getY() {
        return cy;
    }

    public double getZ() {
        return cz;
    }


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


    @SuppressWarnings("boxing")
    public String getAsText() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null) {
            for (Position p : boundary) {
                sb.append(String.format(" (%.5g %.5g %.5g) ", p.getX(), p.getY(), p.getZ()));
            }
        } else {
            sb.append(String.format(" (%.5g %.5g %.5g) ", cx, cy, cz));

        }
        return sb.toString();
    }

    @SuppressWarnings("boxing")
    public String getAsPlainText() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null) {
            for (Position p : boundary) {
                sb.append(String.format(" %.5g %.5g %.5g ", p.getX(), p.getY(), p.getZ()));
            }
        } else {
            sb.append(String.format(" %.5g %.5g %.5g  ", cx, cy, cz));

        }
        return sb.toString();
    }


    @SuppressWarnings("boxing")
    public String getHeadings() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null) {
            for (int i = 0; i < boundary.length; i++) {
                sb.append(" x" + i + " y" + i + " z" + i);

            }
        } else {
            sb.append(" cx cy cz");

        }
        return sb.toString();
    }



    public void setAlongArea(double d) {
        alongArea = d;

    }

    public double getAlongArea() {
        return alongArea;
    }

    public void setSideArea(double d) {
        sideArea = d;
    }
    public double getSideArea() {
        return sideArea;
    }


    public void setTopArea(double d) {
        topArea = d;
    }

    public double getTopArea() {
        return topArea;
    }
}
