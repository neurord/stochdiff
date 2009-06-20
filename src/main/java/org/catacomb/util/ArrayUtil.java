package org.catacomb.util;


public class ArrayUtil {


    public static int getIndexInArray(String s, String[] sa) {
        int iret = -1;

        if (s != null) {
            for (int i = 0; i < sa.length; i++) {
                if (sa[i].equals(s)) {
                    iret = i;
                    break;
                }
            }
        }
        return iret;
    }

    public static double[] copyDArray(double[] xp) {
        int np = xp.length;
        double[] ret = new double[np];
        for (int i = 0; i < np; i++) {
            ret[i] = xp[i];
        }
        return ret;
    }


    public static String[] reverseStringArray(String[] sa) {
        int n = sa.length;
        String[] ret = new String[n];
        for (int i = 0; i < n; i++) {
            ret[i] = sa[n - 1- i];
        }
        return ret;
    }

    public static double minD(double[] d) {
        double ret = 0.;
        if (d != null && d.length > 0) {
            ret = d[0];
            for (int i = 1; i < d.length; i++) {
                if (d[i] < ret) {
                    ret = d[i];
                }
            }
        }
        return ret;
    }


    public static double maxD(double[] d) {
        double ret = 0.;
        if (d != null && d.length > 0) {
            ret = d[0];
            for (int i = 1; i < d.length; i++) {
                if (d[i] > ret) {
                    ret = d[i];
                }
            }
        }
        return ret;
    }



    public static double min(double[][] dd) {
        double ret = Double.NaN;
        if (dd != null) {
            for (int i = 0; i < dd.length; i++) {
                double[] a = dd[i];
                if (a != null) {


                    if (Double.isNaN(ret) && a.length > 0) {
                        ret = a[0];
                    }

                    for (int j = 0; j < a.length; j++) {
                        if (a[j] < ret) {
                            ret = a[j];
                        }
                    }
                }
            }
        }
        return ret;
    }

    public static double max(double[][] dd) {
        double ret = Double.NaN;
        if (dd != null) {
            for (int i = 0; i < dd.length; i++) {
                double[] a = dd[i];
                if (a != null) {


                    if (Double.isNaN(ret) && a.length > 0) {
                        ret = a[0];
                    }

                    for (int j = 0; j < a.length; j++) {
                        if (a[j] > ret) {
                            ret = a[j];
                        }
                    }
                }
            }
        }
        return ret;
    }


    public static double avg(double[] d) {
        double sum = 0.;
        int n = 0;
        if (d != null) {
            for (int i = 0; i < d.length; i++) {
                n += 1;
                sum += d[i];
            }
        }
        double ret = sum / n;
        return ret;
    }


    public static double sd(double[] d) {
        double sumsq = 0.;
        double sum  = 0;
        int n = 0;
        if (d != null) {
            for (int i = 0; i < d.length; i++) {
                n += 1;
                sum += d[i];
                sumsq += d[i] * d[i];
            }
        }
        double avg = sum / n;
        double ret = Math.sqrt(sumsq / n -  avg * avg);
        return ret;

    }




    public static double avg(double[][] dd) {
        double ret = 0.;
        int n = 0;
        if (dd != null) {
            for (int i = 0; i < dd.length; i++) {
                double[] a = dd[i];
                if (a != null) {
                    for (int j = 0; j < a.length; i++) {
                        n += 1;
                        ret += a[j];
                    }
                }
            }
        }
        ret /= n;
        return ret;
    }


    public static double sd(double[][] dd) {
        double sumsq = 0.;
        double sum  = 0;
        int n = 0;
        if (dd != null) {
            for (int i = 0; i < dd.length; i++) {
                double[] a = dd[i];
                if (a != null) {
                    for (int j = 0; j < a.length; i++) {
                        n += 1;
                        sum += a[j];
                        sumsq += a[j] * a[j];
                    }
                }
            }
        }

        double avg = sum / n;
        double ret = Math.sqrt(sumsq - n * avg);
        return ret;

    }


}

