package org.textensor.stochdiff.numeric.morph;

import java.util.ArrayList;

import org.textensor.stochdiff.geom.*;

public class TriangleStrip {


    ArrayList<double[]> wkpts;


    public TriangleStrip() {
        wkpts = new ArrayList<double[]>();
    }


    public void addPoint(double x, double y, double z, double xn, double yn, double zn) {
        // TODO Auto-generated method stub

        double[] wk = {x, y, z, xn, yn, zn};
        wkpts.add(wk);

    }


    public int getLength() {
        return wkpts.size();
    }


    public void rotate(Rotation rot) {
        for (double[] wp : wkpts) {
            Position cp = Geom.position(wp[0], wp[1], wp[2]);

            Position cv = Geom.position(wp[3], wp[4], wp[5]);

            Position rcp = rot.getRotatedPosition(cp);
            Position rcv = rot.getRotatedPosition(cv);

            wp[0] = rcp.getX();
            wp[1] = rcp.getY();
            wp[2] = rcp.getZ();

            wp[3] = rcv.getX();
            wp[4] = rcv.getY();
            wp[5] = rcv.getZ();
        }
    }


    public void translate(Translation trans) {
        for (double[] wp : wkpts) {
            Position cp = Geom.position(wp[0], wp[1], wp[2]);

            Position rcp = trans.getTranslated(cp);

            wp[0] = rcp.getX();
            wp[1] = rcp.getY();
            wp[2] = rcp.getZ();

        }
    }


    public void addPositions(ArrayList<float[]> af) {
        for (double[] w : wkpts) {
            float[] f = new float[3];
            f[0] = (float)w[0];
            f[1] = (float)w[1];
            f[2] = (float)w[2];
            af.add(f);
        }

    }


    public void addNormals(ArrayList<float[]> af) {
        for (double[] w : wkpts) {
            float[] f = new float[3];
            f[0] = (float)w[3];
            f[1] = (float)w[4];
            f[2] = (float)w[5];
            af.add(f);
        }
    }


    public void flip() {
        ArrayList<double[]> wpn = new ArrayList<double[]>();
        for (int i = 0; i < wkpts.size(); i+= 2) {
            wpn.add(wkpts.get(i+1));
            wpn.add(wkpts.get(i));
        }
        wkpts = wpn;
    }


}
