package org.catacomb.numeric.array;

import org.catacomb.report.E;

// MultiDimensionalArrays
public class MDA {


    public static double[] concatenate(double[][] dda) {
        int n = 0;
        for (int i = 0; i < dda.length; i++) {
            n += dda[i].length;
        }
        double[] ret = new double[n];
        n = 0;
        for (int i = 0; i < dda.length; i++) {
            double[] sa =dda[i];
            for (int j = 0; j < sa.length; j++) {
                ret[n++] = sa[j];
            }
        }
        return ret;
    }



    public static double prod(double[] a) {
        double ret = 1.;
        for (int i = 0; i < a.length; i++) {
            ret = ret * a[i];
        }
        return ret;
    }

    public static double[] prod(double a, double[] b) {
        int n = b.length;
        double[] ret = new double[n];
        for (int i = 0; i < n; i++) {
            ret[i] = a * b[i];
        }
        return ret;
    }

    public static double[] prod(double[] a, double[] b) {
        double[] ret = null;
        if (a.length == b.length) {
            int nel = a.length;
            ret = new double[nel];
            for (int i = 0; i < nel; i++) {
                ret[i] = a[i] * b[i];
            }
        } else {
            E.error("not conformable " + a.length + " " + b.length);
        }
        return ret;
    }



    public static double[][] prod(double[][] a, double[][] b) {
        double[][] ret = null;
        if (a.length == b.length) {
            int nel = a.length;
            ret = new double[nel][];
            for (int i = 0; i < nel; i++) {
                ret[i] = prod(a[i], b[i]);
            }
        } else {
            E.error("not conformable " + a.length + " " + b.length);
        }
        return ret;
    }




    public static double[] sum(double[][] a) {
        double[] ret = new double[a.length];
        for (int i = 0; i < a.length; i++) {
            ret[i] = sum(a[i]);
        }
        return ret;
    }




    public static double sum(double[] a) {
        double ret = 0.;
        for (int i = 0; i < a.length; i++) {
            ret += a[i];
        }
        return ret;
    }

    public static double avg(double[] a) {
        return (sum(a) / a.length);
    }

}
