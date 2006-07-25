package org.textensor.stochdiff.numeric.morph;

import org.textensor.report.E;
import org.textensor.stochdiff.geom.Position;


import java.util.ArrayList;


public class TreePoint implements Position {

    public double x;
    public double y;
    public double z;
    public double r;

    public TreePoint[] nbr;
    public int nnbr;

    public String region;
    public String region1; // for borders onto other regions;
    public String region2;

    public String label;

    // temporary work variables
    public int iwork;
    public boolean dead; // marking prior to removal


    // x and y positions when part of a dendrogram, set by MLC
    public double dgx;
    public double dgy;


    private ArrayList<TreePoint> offsetChildren;

    public String name; // not sure about this POSERR;


    public TreePoint() {
        nbr = new TreePoint[6];
        nnbr = 0;
        dead = false;

    }



    public TreePoint(double x, double y, double z, double r) {
        this();
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
    }


    public double getRadius() {
        return r;
    }


    public double getX() {
        return x;
    }


    public double getY() {
        return y;
    }


    public double getZ() {
        return z;
    }

    public String getLabel() {
        return label;
    }


    public String getRegion() {
        return region;
    }


    public TreePoint makeCopy() {
        return new TreePoint(x, y, z, r);
    }


    public void setWork(int iw) {
        iwork = iw;
    }


    public int getWork() {
        return iwork;
    }


    public String toString() {
        return ("zyzr: " + x + " " + y + " " + z + " " + r + " nnbr=" + nnbr);
    }


    public void setPosition(double[] a) {
        x = a[0];
        y = a[1];
        z = a[2];
        if (a.length >= 3) {
            r = a[3];
        }
    }


    public void locateBetween(TreePoint cpa, TreePoint cpb, double f) {
        double wf = 1. - f;
        x = f * cpb.x + wf * cpa.x;
        y = f * cpb.y + wf * cpa.y;
        z = f * cpb.z + wf * cpa.z;
        r = f * cpb.r + wf * cpa.r;
    }


    // REFAC - these should all be private, so only the
    // static methods that presever symmetry are visible
    public void addNeighbor(TreePoint cpn) {
        boolean has = false;
        for (int i = 0; i < nnbr; i++) {
            if (nbr[i] == cpn) {
                E.error("adding a neighbor we already have ");
                has = true;
            }
        }
        if (has) {
            // do nothing more - shouldn't have been called though;

        } else {
            if (nnbr >= nbr.length) {
                TreePoint[] pn = new TreePoint[2 * nnbr];
                for (int i = 0; i < nnbr; i++) {
                    pn[i] = nbr[i];
                }
                nbr = pn;
            }
            nbr[nnbr++] = cpn;
        }
    }


    public void removeNeighbor(TreePoint cp) {
        int ii = -1;
        for (int i = 0; i < nnbr; i++) {
            if (nbr[i] == cp) {
                ii = i;
            }
        }
        if (ii >= 0) {
            for (int i = ii; i < nnbr - 1; i++) {
                nbr[i] = nbr[i + 1];
            }
            nnbr--;
        }
    }


    public void replaceNeighbor(TreePoint cp, TreePoint cr) {

        int ii = -1;
        for (int i = 0; i < nnbr; i++) {
            if (nbr[i] == cp) {
                ii = i;
            }
        }
        if (ii >= 0) {
            nbr[ii] = cr;
        } else {
            E.error(" (replaceNeighbor) couldnt find nbr " + cp + " in nbrs list of " + this);
        }
    }


    public boolean hasNeighbor(TreePoint cp) {
        boolean hn = false;
        for (int i = 0; i < nnbr; i++) {
            if (nbr[i] == cp) {
                hn = true;
            }
        }
        return hn;
    }


    public void removeDeadNeighbors() {
        for (int i = nnbr - 1; i >= 0; i--) {
            if (nbr[i].dead) {
                removeNeighbor(nbr[i]);
            }
        }
    }


    // these are branches that start some way down a segment, but are
    // linked from here temporarily until the tree is discretized and a new point
    // is available to have them connected from as neighbors
    public void addOffsetChild(TreePoint p) {
        if (offsetChildren == null) {
            offsetChildren = new ArrayList<TreePoint>();
        }
        offsetChildren.add(p);
    }


    public boolean hasOffsetChildren() {
        return (offsetChildren != null);
    }


    public ArrayList<TreePoint> getOffsetChildren() {
        return offsetChildren;
    }


    public double distanceTo(TreePoint cp) {
        double dx = x - cp.x;
        double dy = y - cp.y;
        double dz = z - cp.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }


    public void movePerp(TreePoint ca, TreePoint cb, double dperp) {
        double dx = cb.x - ca.x;
        double dy = cb.y - ca.y;
        double f = Math.sqrt(dx * dx + dy * dy);
        dx /= f;
        dy /= f;
        x += dperp * dy;
        y -= dperp * dx;
    }


    public static void neighborize(TreePoint tp, TreePoint tpn) {
        tp.addNeighbor(tpn);
        tpn.addNeighbor(tp);

    }



    public ArrayList<TreePoint> getNeighbors() {
        ArrayList<TreePoint> ret = new ArrayList<TreePoint>();
        for (int i = 0; i < nnbr; i++) {
            ret.add(nbr[i]);
        }
        return ret;
    }


    public boolean isEndPoint() {
        boolean ret = false;
        if (nnbr == 1) {
            ret = true;
        }
        return ret;
    }



    public TreePoint oppositeNeighbor(TreePoint tpp) {
        TreePoint ret = null;
        if (nnbr == 2) {
            if (nbr[0] == tpp) {
                ret = nbr[1];
            } else {
                ret = nbr[0];
            }
        }
        return ret;
    }



    public void setRegion(String s) {
        region = s;
    }

    public void setRegion1(String s) {
        region1 = s;
    }
    public void setRegion2(String s) {
        region2 = s;
    }

    public boolean bordersRegion(String s) {
        boolean ret = false;
        if (s != null) {
            if (s.equals(region) || s.equals(region1) || s.equals(region2)) {
                ret = true;
            }
        }
        return ret;
    }


    public void setLabel(String s) {
        label = s;
    }


}
