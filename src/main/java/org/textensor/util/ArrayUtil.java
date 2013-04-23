package org.textensor.util;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ArrayUtil {
    static final Logger log = LogManager.getLogger(ArrayUtil.class);

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

    public static int maxLength(int[][]... array) {
        int i = 0;
        for (int[][] a: array)
            for (int[] b: a)
                if (b.length > i)
                    i = b.length;
        return i;
    }

    public static double[] flatten(double[][] array, int columns) {
        double[] flat = new double[array.length * columns];
        return _flatten(flat, array, columns);
    }

    public static double[] _flatten(double[] flat,
                                    double[][] array, int columns) {
        for (int i = 0; i < array.length; i++)
            System.arraycopy(array[i], 0, flat, i * columns, array[i].length);
        return flat;
    }

    public static int[] flatten(int[][] array, long columns, int fill) {
        int[] flat = new int[array.length * (int) columns];
        return _flatten(flat, array, columns, fill);
    }

    public static int[] _flatten(int[] flat,
                                 int[][] array, long columns, int fill) {
        assert flat.length == array.length * columns;
        for (int i = 0; i < array.length; i++) {
            System.arraycopy(array[i], 0, flat, i * (int) columns, array[i].length);
            if (array[i].length < columns)
                /* work around apparent bug in Arrays.fill, where writing an
                 * empty range to the end of flat fails. */
                Arrays.fill(flat,
                            i * (int) columns + array[i].length,
                            (i + 1) * (int) columns,
                            fill);
        }
        return flat;
    }


    public static int[] flatten(int[][][] array, long columns, int fill) {
        int rows = array.length > 0 ? array[0].length : 0;
        int[] flat = new int[array.length * rows * (int) columns];
        return _flatten(flat, array, columns, fill);
    }

    public static int[] _flatten(int[] flat,
                                    int[][][] array, long columns, int fill) {
        int rows = array.length > 0 ? array[0].length : 0;
        assert flat.length == array.length * rows * columns;

        for (int i = 0; i < array.length; i++) {
            assert array[i].length == rows;
            for (int j = 0; j < rows; j++) {
                int offset = (i * rows + j) * (int) columns;
                // System.out.println("" + i + " " + j + " " + offset + " " +
                //                    array[i][j].length);
                System.arraycopy(array[i][j], 0, flat, offset,
                                 array[i][j].length);
                Arrays.fill(flat, offset + array[i][j].length,
                            offset + (int) columns, fill);
            }
        }
        return flat;
    }

    public static String xJoined(int[] dims) {
        if (dims.length == 0)
            return "";
        String s = "" + dims[0];
        for (int i = 1; i < dims.length; i++)
            s += "×" + dims[i];
        return s;
    }

    public static String xJoined(long[] dims) {
        if (dims.length == 0)
            return "";
        String s = dims[0] == -1 ? "∞" : "" + dims[0];
        for (int i = 1; i < dims.length; i++)
            s += "×" + (dims[i] == -1 ? "∞" : "" + dims[i]);
        return s;
    }

    public static int[] flatten(int[] flat,
                                   int[][][] array, int columns, int fill) {
        try {
            return _flatten(flat, array, columns, fill);
        } catch(RuntimeException e) {
            log.error("flat[{}] vs. array[{}x{}x{}], columns={}",
                      flat.length,
                      array.length,
                      array.length > 0 ? array[0].length : '?',
                      array.length > 0 && array[0].length > 0 ? array[0][0].length : '?',
                      columns);
            throw e;
        }
    }

    public static double[] log(double[] d) {
        double[] ret = new double[d.length];
        for (int i = 0; i < d.length; i++)
            ret[i] = d[i] == 0 ? Float.NEGATIVE_INFINITY : Math.log(d[i]);
        return ret;
    }

    public static double[][] log(double[][] d) {
        double[][] ret = new double[d.length][];
        for (int i = 0; i < d.length; i++)
            ret[i] = log(d[i]);
        return ret;
    }

    public static double[] logArray(int len) {
        double[] ret = new double[len];
        ret[0] = Float.NEGATIVE_INFINITY;
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
        if (a.length != b.length)
            return false;

        for (int i = 0; i < a.length; i++)
            if (!a[i].equals(b[i]))
                return false;

        return true;
    }

    public static void copy(int[][] src, int dst[][]) {
        assert src.length == dst.length;
        for(int i = 0; i < src.length; i++)
            System.arraycopy(src[i], 0, dst[i], 0, dst[i].length);
    }

    public static void copy(double[][] src, double dst[][]) {
        assert src.length == dst.length;
        for(int i = 0; i < src.length; i++)
            System.arraycopy(src[i], 0, dst[i], 0, dst[i].length);
    }

    public static void fill(int[][] array, int value) {
        for (int[] subarray: array)
            Arrays.fill(subarray, value);
    }

    public static void fill(double[][] array, double value) {
        for (double[] subarray: array)
            Arrays.fill(subarray, value);
    }

    public static void fill(int[][][] arrays, int value) {
        for (int[][] array: arrays)
            for (int[] subarray: array)
                Arrays.fill(subarray, value);
    }

    public static int[][] shape(int[] flat, int rows, int cols) {
        int[][] ans = new int[rows][cols];
        for (int i = 0; i < rows; i++)
            System.arraycopy(flat, rows * i, ans[i], 0, cols);
        return ans;
    }

    public static double[][] shape(double[] flat, int rows, int cols) {
        double[][] ans = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            System.arraycopy(flat, rows * i, ans[i], 0, cols);
        return ans;
    }
}
