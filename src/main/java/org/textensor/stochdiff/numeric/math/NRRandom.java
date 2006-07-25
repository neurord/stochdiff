

package org.textensor.stochdiff.numeric.math;


public final class NRRandom {


    // This is one of Numerical Recipe's "quick and dirty" generators
    // for when you don't need very many samples.
    // in java, its actually no faster than the mersenne twister.

    private static int jran = 12345;
    private static int im = 714025;
    private static int ia = 1366;
    private static int ic = 150889;


    // A = 1664525, B = 1013904223, M = 2^32




    public final static double random() {
        jran = (jran * ia + ic) % im;
        double ran = (1. * jran) / im;
        return ran;
    }

    public static int getSeed() {
        return jran;
    }


    public static void setSeed(int jr) {
        jran = jr;
    }



    public static double nextRandom() {
        return random();
    }


    public static double uniformRV() {
        return random();
    }




    public static int weightedSample(double[] rw) {
        int n = rw.length;
        double a = random();
        int inew = 0;
        while ((a -= rw[inew]) > 0 && inew < n-1) {
            inew++;
        }
        return inew;
    }



    public  static double gaussianRV() {
        return grv();
    }


    public static double  grv() {
        double r, ran1, ran2, fac, g1;
        r = -1;
        ran1 = 0.0;
        ran2 = 0.0;
        while (r <= 0.0 || r >= 1.0) {
            jran = (jran * ia + ic) % im;
            ran1 = (2. * jran) / im - 1;

            jran = (jran * ia + ic) % im;
            ran2 = (2. * jran) / im - 1;

            r = ran1 * ran1 + ran2 * ran2;
        }
        fac = Math.sqrt(-2. * Math.log(r) / r);
        g1 = ran1 * fac;
        //      g2 = ran2 * fac;
        return g1;
    }



}




