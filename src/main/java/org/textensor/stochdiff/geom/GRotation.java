package org.textensor.stochdiff.geom;

import org.textensor.report.E;


public class GRotation implements Rotation {

    public static final String Z_AXIS = "zaxis";
    public static final String Y_AXIS = "yaxis";
    double[][] mtx;


    public GRotation() {
        mtx = new double[3][3];
        for (int i = 0; i < 3; i++) {
            mtx[i][i] = 1.;
        }
    }


    public GRotation(String axis, double angle) {
        this();
        double ca = Math.cos(angle);
        double sa = Math.sin(angle);

        if (axis.equals(Z_AXIS)) {
            mtx[0][0] = ca;
            mtx[0][1] = -sa;
            mtx[1][0] = sa;
            mtx[1][1] = ca;

        } else if (axis.equals(Y_AXIS)) {
            mtx[0][0] = ca;
            mtx[0][2] = -sa;
            mtx[2][0] = sa;
            mtx[2][2] = ca;


        } else {
            E.error("unknown axis " + axis);
        }
    }





    public Vector getRotatedVector(Vector v) {
        double[] d = {v.getDX(), v.getDY(), v.getDZ()};
        double[] r = new double[3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                r[i] += mtx[i][j] * d[j];
            }
        }
        return new GVector(r);
    }


    public Position getRotatedPosition(Position p, Position pcenter) {
        Vector v = Geom.fromToVector(pcenter, p);
        Vector vnew = getRotatedVector(v);
        Translation trans = new GTranslation(vnew);
        return trans.getTranslated(pcenter);
    }


    public void rotateAbout(Movable mov, Position pcenter) {
        // TODO Auto-generated method stub

    }


    public GRotation times(GRotation gr1) {
        GRotation ret = new GRotation();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ret.mtx[i][j] = 0.;
                for (int k = 0; k < 3; k++) {
                    ret.mtx[i][j] += mtx[i][k] * gr1.mtx[k][j];
                }
            }
        }
        return ret;
    }


    public Position getRotatedPosition(Position p) {
        Vector v = Geom.getToVector(p);
        Vector vr = getRotatedVector(v);
        return Geom.endPosition(vr);
    }


}
