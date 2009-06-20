

package org.catacomb.numeric.difnet;

import org.catacomb.report.E;



public class CellPoint {
    public int activeType;
    public int geometricalType;

    public double x;
    public double y;
    public double z;
    public double r;

    public CellPoint[] nbr;
    public int nnbr;


    // temporary variables for indexing a structure from a given starting pt;
    public int index;
    public int parentIndex;

    // temporary work variables
    public int iwork;
    public boolean umark;  // user marked points;
    public boolean wmark;  // own working marks;
    public boolean dead;   // marking prior to removal
    public int wcount;     // for counting contacts when tracing loops


    // x and y positions when part of a dendrogram, set by MLC
    public double dgx;
    public double dgy;


    public String name;  // not sure about this POSERR;

    public CellPoint() {
        nbr = new CellPoint[6];
        nnbr = 0;
        dead = false;
        umark = false;
        wmark = false;
    }


    public CellPoint(double x, double y, double z, double r,
                     int gtype, int atype) {
        this();
        this.x = x;
        this.y = y;
        this.z = z;
        this.r = r;
        this.geometricalType = (gtype < 0 ? 0 : gtype);
        this.activeType = (atype < 0 ? 0 : atype);
    }


    public CellPoint makeCopy() {
        return new CellPoint(x, y, z, r, geometricalType, activeType);
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


    public void locateBetween(CellPoint cpa, CellPoint cpb, double f) {
        double wf = 1. - f;
        x = f * cpb.x + wf * cpa.x;
        y = f * cpb.y + wf * cpa.y;
        z = f * cpb.z + wf * cpa.z;
        r = f * cpb.r + wf * cpa.r;
    }



    public void addNeighbor(CellPoint cpn) {
        if (nnbr >= nbr.length) {
            CellPoint[] pn = new CellPoint[2*nnbr];
            for (int i = 0; i < nnbr; i++) {
                pn[i] = nbr[i];
            }
            nbr = pn;
        }
        nbr[nnbr++] = cpn;
    }


    public void removeNeighbor(CellPoint cp) {
        int ii = -1;
        for (int i = 0; i < nnbr; i++) {
            if (nbr[i] == cp) {
                ii = i;
            }
        }
        if (ii >= 0) {
            for (int i = ii; i < nnbr-1; i++) {
                nbr[i] = nbr[i+1];
            }
            nnbr--;
        }
    }


    public void replaceNeighbor(CellPoint cp, CellPoint cr) {

        int ii = -1;
        for (int i = 0; i < nnbr; i++) {
            if (nbr[i] == cp) {
                ii = i;
            }
        }
        if (ii >= 0) {
            nbr[ii] = cr;
        } else {
            E.error(" (replaceNeighbor) couldnt find nbr " + cp +
                    " in nbrs list of " + this);
        }
    }


    public boolean hasNeighbor(CellPoint cp) {
        boolean hn = false;
        for (int i = 0; i < nnbr; i++) {
            if (nbr[i] == cp) {
                hn = true;
            }
        }
        return hn;
    }


    public void removeDeadNeighbors() {
        for (int i = nnbr-1; i >= 0; i--) {
            if (nbr[i].dead) {
                removeNeighbor(nbr[i]);
            }
        }
    }


    public double distanceTo(CellPoint cp) {
        double dx = x - cp.x;
        double dy = y - cp.y;
        double dz = z - cp.z;
        return Math.sqrt(dx*dx + dy*dy + dz*dz);
    }


    public void movePerp(CellPoint ca, CellPoint cb, double dperp) {
        double dx = cb.x - ca.x;
        double dy = cb.y - ca.y;
        double f = Math.sqrt(dx*dx + dy*dy);
        dx /= f;
        dy /= f;
        x += dperp * dy;
        y -= dperp * dx;
    }


}






