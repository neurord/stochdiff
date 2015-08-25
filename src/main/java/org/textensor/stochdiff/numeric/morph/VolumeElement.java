//6 18 2007: WK added a boolean variable (submembrane), and three functions (isSubmembrane, getSubmembrane, and setSubmembrane)
//written by Robert Cannon
package org.textensor.stochdiff.numeric.morph;

import java.util.ArrayList;

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

    double alongArea;
    double sideArea;
    double topArea;

    ArrayList<ElementConnection> connections;

    Position[] boundary;

    Position[] surfaceBoundary = null;

    boolean fixcon = false;


    boolean submembrane = false; //true ifn this volume element lies on submembrane


    String groupID = null;


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
        assert !fixcon;
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

    public String getAsText() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null)
            for (Position p : boundary)
                sb.append(String.format("%s(%.5g %.5g %.5g)",
                                        sb.length() > 0 ? " " : "",
                                        p.getX(), p.getY(), p.getZ()));
        else
            sb.append(String.format("%s(%.5g %.5g %.5g)",
                                    sb.length() > 0 ? " " : "",
                                    cx, cy, cz));
        return sb.toString();
    }

    public String getHeadings() {
        StringBuffer sb = new StringBuffer();
        // export boundary if have it, ow just the center point;
        if (boundary != null)
            for (int i = 0; i < boundary.length; i++)
                sb.append(" x" + i + " y" + i + " z" + i);
        else
            sb.append(" cx cy cz");

        sb.append(" volume deltaZ");
        return sb.toString();
    }

    public String getAsPlainText() {
        StringBuffer sb = new StringBuffer();
        for(double p: this.getAsNumbers())
            sb.append(String.format("%s%.5g", sb.length() > 0 ? " " : "", p));
        return sb.toString();
    }

    public double[] getAsNumbers() {
        // export boundary if have it, or just the center point;
        if (boundary != null) {
            double ans[] = new double[3 * boundary.length + 2];
            int i = 0;
            for (Position p: boundary) {
                ans[i++] = p.getX();
                ans[i++] = p.getY();
                ans[i++] = p.getZ();
            }
            ans[i++] = volume;
            ans[i++] = deltaZ;
            assert i == ans.length;
            return ans;
        } else {
            return new double[]{cx, cy, cz, volume, deltaZ};
        }
    }

    public void setGroupID(String lroot) {
        groupID = lroot;

    }

    public String getGroupID() {
        return groupID;
    }

}
