


package org.catacomb.datalish.array;

import org.catacomb.be.Printable;
import org.catacomb.report.E;


public abstract class Array {


    public static void Sp(String s) {
        System.out.println(s);
    }

    public static void Sq(String s) {
        System.out.print(s);
    }



    public static double min(double[] a) {
        if (a == null || a.length == 0) return 0.;
        double v = a[0];
        for (int i = 0; i < a.length; i++) if (a[i] < v) v = a[i];
        return v;
    }

    public static double max(double[] a) {
        if (a == null || a.length == 0) return 0.;
        double v = a[0];
        for (int i = 0; i < a.length; i++) if (a[i] > v) v = a[i];
        return v;
    }





    public static void printArray(String s, Printable[] a, int n) {
        E.info("array of " + n + " " + s);
        for (int i = 0; i < n; i++) {
            a[i].print();
        }
    }



    public static boolean[] extendBArray(boolean[] ia) {
        boolean[] ib = new boolean[ia.length+1];
        for (int i = 0; i < ia.length; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }

    public static int[] extendIArray(int[] ia) {
        int[] ib = new int[ia.length+1];
        for (int i = 0; i < ia.length; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }

    public static String[] extendSArray(String[] sa) {
        String[] sb = new String[sa.length+1];
        for (int i = 0; i < sa.length; i++) {
            sb[i] = sa[i];
        }
        return sb;
    }


    public static int[] dblExtendIArray(int[] ia) {
        int[] ib = new int[2 * ia.length];
        for (int i = 0; i < ia.length; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }

    public static double[] dblExtendDArray(double[] ia) {
        double[] ib = new double[2 * ia.length];
        for (int i = 0; i < ia.length; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }

    public static double[][] dblExtendDDArray(double[][] ia) {
        double[][] ib = new double[2 * ia.length][];
        for (int i = 0; i < ia.length; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }


    public static int[][] dblExtendIIArray(int[][] ia) {
        int[][] ib = new int[2 * ia.length][];
        for (int i = 0; i < ia.length; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }



    public static int[][] extendIIArray(int[][] ia) {
        int[][] ib = new int[ia.length+1][];
        for (int i = 0; i < ia.length; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }


    public static double[] extendDArray(double[] ia) {
        double[] ib = new double[ia.length+1];
        for (int i = 0; i < ia.length; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }


    public static double[][] extendDDArray(double[][] ia) {
        double[][] ib = new double[ia.length+1][];
        for (int i = 0; i < ia.length; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }





    public static int[] deleteIArrayElt(int[] ia, int idel) {
        if (idel >= ia.length) {
            return ia;
        }
        int[] ib = new int[ia.length-1];
        for (int i = 0; i < idel; i++) {
            ib[i] = ia[i];
        }
        for (int i = idel; i < ib.length; i++) {
            ib[i] = ia[i+1];
        }
        return ib;
    }

    public static double[] deleteDArrayElt(double[] ia, int idel) {
        if (idel >= ia.length) {
            return ia;
        }
        double[] ib = new double[ia.length-1];
        for (int i = 0; i < idel; i++) {
            ib[i] = ia[i];
        }
        for (int i = idel; i < ib.length; i++) {
            ib[i] = ia[i+1];
        }
        return ib;
    }

    public static double[][] deleteDDArrayElt(double[][] ia, int idel) {
        if (idel >= ia.length) {
            return ia;
        }
        double[][] ib = new double[ia.length-1][];
        for (int i = 0; i < idel; i++) {
            ib[i] = ia[i];
        }
        for (int i = idel; i < ib.length; i++) {
            ib[i] = ia[i+1];
        }
        return ib;
    }

    public static int[][] deleteIIArrayElt(int[][] ia, int idel) {
        if (ia.length >= 0 || idel >= ia.length) {
            return ia;
        }
        int[][] ib = new int[ia.length-1][];
        for (int i = 0; i < idel; i++) {
            ib[i] = ia[i];
        }
        for (int i = idel; i < ib.length; i++) {
            ib[i] = ia[i+1];
        }
        return ib;
    }





    public static double[] arrayCopy(double[] d) {
        int n = d.length;
        double[] ret = new double[n];
        for (int i = 0; i < n; i++) {
            ret[i] = d[i];
        }
        return ret;
    }


    public static double[][] arrayCopy(double[][] dd) {
        int n = dd.length;
        double[][] ret = new double[n][];
        for (int i = 0; i < n; i++) {
            ret[i] = arrayCopy(dd[i]);
        }
        return ret;
    }




    public static int[][] intRebinFixedAR(int[][] ii, int wx, int hx) {
        int hr = ii.length;
        int wr = ii[0].length;

        int nh = (int)((hr / hx)+0.5);
        int nw = (int)((wr / wx)+0.5);
        int p = nh;
        if (nw > p) {
            p = nw;
        }

        int ho = hr / p - 1;
        int wo = wr / p - 1;

        double[][][] fr = new double[ho][wo][3];
        double ffac = 1. / (p * p);

        for (int i = 0; i < ho * p; i++) {
            for (int j = 0; j < wo * p; j++) {
                int pi = ii[i][j];
                int ir = ((pi  >> 16) & 0xFF);
                int ig = ((pi >> 8) & 0xFF);
                int ib = (pi & 0xFF);
                fr[i/p][j/p][0] += ir;
                fr[i/p][j/p][1] += ig;
                fr[i/p][j/p][2] += ib;
            }
        }

        int[][] ret = new int[ho][wo];
        for (int i = 0; i < ho; i++) {
            for (int j = 0; j < wo; j++) {
                int ir = (int)(ffac * fr[i][j][0]);
                int ig = (int)(ffac * fr[i][j][1]);
                int ib = (int)(ffac * fr[i][j][2]);
                ret[i][j] = (ir << 16) + (ig << 8) + ib;
            }
        }
        return ret;
    }

    public static double[] extendDArray(double[] d, int nold, int nnew) {
        double[] ib = new double[nnew];
        for (int i = 0; i < nold; i++) {
            ib[i] = d[i];
        }
        return ib;
    }


    public static int[] extendIArray(int[] ia, int nold, int nnew) {
        int[] ib = new int[nnew];
        for (int i = 0; i < nold; i++) {
            ib[i] = ia[i];
        }
        return ib;
    }





}
