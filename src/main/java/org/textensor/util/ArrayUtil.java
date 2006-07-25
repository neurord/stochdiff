package org.textensor.util;


public class ArrayUtil {


    public static double[] log(double[] d) {
        double[] ret = new double[d.length];
        for (int i = 0; i < d.length; i++) {
            ret[i] = Math.log(d[i]);
        }
        return ret;
    }

    public static double[][] log(double[][] d) {
        double[][] ret = new double[d.length][];
        for (int i = 0; i < d.length; i++) {
            ret[i] = log(d[i]);
        }
        return ret;
    }


    public static int findBracket(double[] v, double x) {
        if (v == null || v.length == 0) {
            return -1;
        }

        int n = v.length;
        int ret = 0;
        if (x < v[0]) {
            ret = -1;
        } else if (x > v[n-1]) {
            ret = n-1;
        } else {

            int ia = 0;
            int ib = n-1;
            while (ib - ia > 1) {
                int ic = (ia + ib) / 2;
                if (x <= v[ic]) {
                    ib = ic;
                } else {
                    ia = ic;
                }
            }
            ret = ia;
        }
        return ret;
    }

    public static double[] span(double xa, double xb, int nel) {
        double[] ret = new double[nel+1];
        double dx = (xb - xa) / nel;
        for (int i = 0; i < nel+1; i++) {
            ret[i] = xa + i * dx;
        }
        return ret;
    }

    public static double[] interpInAtFor(double[] aw, double[] ax, double[] xbd) {
        int np = xbd.length;
        int ns = ax.length;

        double[] ret = new double[np];
        for (int i = 0; i < np; i++) {
            double x = xbd[i];

            // REFAC - could be smarter if sure xbd monotonic;
            int ipos = 0;
            while (ipos < ns-2 && x > ax[ipos+1]) {
                ipos += 1;
            }
            double f = (x - ax[ipos]) / (ax[ipos+1] - ax[ipos]);

            ret[i] = f * aw[ipos+1] + (1.-f) * aw[ipos];

        }
        return ret;

    }

}
