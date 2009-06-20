package org.catacomb.numeric.data;


public final class DoubleArray {


    double[] data;
    int npoint;


    public DoubleArray() {
        data = new double[100];
        npoint = 0;
    }

    public DoubleArray(double[] da) {
        data = da;
        npoint = da.length;
    }


    public int getNPoint() {
        return npoint;
    }

    public double[] getData() {
        return data;
    }


    public void addPoint(double d) {
        if (data == null) {
            data = new double[100];
        }

        if (npoint == data.length) {
            double[] td = new double[2 * data.length];
            System.arraycopy(data, 0, td, 0, npoint);
            data = td;
        }
        data[npoint++] = d;
    }


    public double getValue(int n) {
        return (n >= 0 && n < npoint ? data[n] : 0.);
    }

    public double[] getCutDownArray() {
        double[] da = new double[npoint];
        System.arraycopy(data, 0, da, 0, npoint);
        return da;
    }
}
