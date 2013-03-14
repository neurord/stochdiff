package org.textensor.util;

import java.util.Arrays;

public class ArrayUtil {

    public static int maxLength(String... array) {
        int i = 0;
        for (String s: array)
            if (s.length() > i)
                i = s.length();
        return i;
    }

    public static int maxLength(double[]... array) {
        int i = 0;
        for (double[] a: array)
            if (a.length > i)
                i = a.length;
        return i;
    }

    public static int maxLength(int[]... array) {
        int i = 0;
        for (int[] a: array)
            if (a.length > i)
                i = a.length;
        return i;
    }

    public static double[] flatten(double[][] array, int columns) {
        double[] flat = new double[array.length * columns];
        for (int i = 0; i < array.length; i++)
            System.arraycopy(array[i], 0, flat, i * columns, array[i].length);
        return flat;
    }

    public static int[] flatten(int[][] array, int columns, int fill) {
        int[] flat = new int[array.length * columns];
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, flat, i * columns, array[i].length);
            Arrays.fill(flat, i * columns + array[i].length, (i + 1) * columns,
                        fill);
        }
        return flat;
    }

    public static double[] log(double[] d, double dzero) {
        double[] ret = new double[d.length];
        for (int i = 0; i < d.length; i++)
            ret[i] = d[i] <= 0 ? dzero : Math.log(d[i]);
        return ret;
    }

    public static double[][] log(double[][] d, double dzero) {
        double[][] ret = new double[d.length][];
        for (int i = 0; i < d.length; i++)
            ret[i] = log(d[i], dzero);
        return ret;
    }

    public static double[] logArray(int len, double zeroth) {
        double[] ret = new double[len];
        ret[0] = zeroth;
        for (int i = 1; i < ret.length; i++)
            ret[i] = Math.log(i);
        return ret;
    }

    public static int findBracket(double[] v, double x) {
        if (v == null || v.length == 0)
            return -1;

        int n = v.length;
        int ret = 0;
        /*AB: v[0] is cumulative.  Thus, if x < v[0] the spine goes into region 0 (don't exit).
         * Also, if x > v[n-1] that is bad - exit (don't return n-1) */
        if (x <= v[0]) {
            ret = 0;
        } else if (x > v[n-1]) {
            ret = -1;
        } else {

            /* AB, jan 25, 2012: this part used x<= v[ic] (num methods uses x>=v[ic], and found the wrong index */
            int ia = 0;
            int ib = n-1;
            while (ib - ia > 1) {
                int ic = (ia + ib) / 2;
                if (x >= v[ic]) {
                    ia = ic;
                } else {
                    ib = ic;
                }
            }
            /* AB, jan 25, 2012: ia+1 corrects for ia being the lower limit in a cumulative array */
            ret = ia+1;
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

    public static boolean arraysMatch(String[] a, String[] b) {
        boolean ret = false;
        if (a.length == b.length) {
            ret = true;
            for (int i = 0; i < a.length; i++) {
                if (a[i].equals(b[i])) {
                    // ok
                } else {
                    ret = false;
                }
            }

        }

        return ret;
    }

    public static void copy(int[][] src, int dst[][]) {
        assert src.length == dst.length;
        for(int i = 0; i < src.length; i++)
            System.arraycopy(src[i], 0, dst[i], 0, dst[i].length);
    }
}
