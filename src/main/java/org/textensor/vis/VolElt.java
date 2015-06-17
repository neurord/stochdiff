package org.textensor.vis;

public class VolElt {

    int[] lens;
    float[] verts;
    float[] norms;


    double cx;
    double cy;
    double cz;

    public VolElt(int[] l, float[] v, float[] n) {
        lens = l;
        verts = v;
        norms = n;
        //E.info("set v " + v[0] + " " + v[1] + " " + v[2]);
    }

    public int getNvert() {
        return verts.length;
    }

    public int[] getLens() {
        return lens;
    }

    public float[] getVerts() {
        return verts;
    }

    public float[] getNorms() {
        return norms;
    }


    public void centroidize() {
        int nv = verts.length / 3;
        cx = 0;
        cy = 0;
        cz = 0;
        for (int i = 0; i < nv; i++) {
            cx += verts[3 * i];
            cy += verts[3 * i + 1];
            cz += verts[3 * i + 2];
        }
        cx /= nv;
        cy /= nv;
        cz /= nv;


        for (int i = 0; i < nv; i++) {
            verts[3 * i] -= cx;
            verts[3 * i + 1] -= cy;
            verts[3 * i + 2] -= cz;
        }
    }

    public double getCX() {
        return cx;
    }

    public double getCY() {
        return cy;
    }

    public double getCZ() {
        return cz;
    }
}
