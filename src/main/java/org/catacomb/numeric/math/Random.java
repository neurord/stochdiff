

package org.catacomb.numeric.math;


public final class Random {

    private static int jran = 12345;
    private static int im = 714025;
    private static int ia = 1366;
    private static int ic = 150889;



    private static double[] cof = {76.18009173, -86.50532033, 24.01409822,
                                   -1.231739516, 0.120858003e-2, -0.536382e-5
                                  };




    public static double random() {
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



    public final static int poissonInt(double d) {
        return (int)(poidev(d));
    }



    public final static double gammln(double xx) {
        double x = xx - 1.0;
        double tmp = x + 5.5;
        tmp -= (x+0.5) * Math.log(tmp);
        double ser = 1.0;
        for (int j = 0; j <= 5; j++) {
            x += 1.0;
            ser += cof[j]/x;
        }
        return -tmp+Math.log(2.50662827465*ser);
    }



    public final static double poidev(double xm) {

        double em = -1.;
        if (xm < 12.0) {
            double g = Math.exp(-xm);
            double t = 1.0;
            do {
                em += 1.0;
                t *= uniformRV();
            } while (t > g);


        } else {
            double sq = Math.sqrt(2.0 * xm);
            double alxm = Math.log(xm);
            double g = xm * alxm - gammln(xm + 1.0);
            double y = 0.;
            double t = -1.;
            do {
                do {
                    y = Math.tan(Math.PI*uniformRV());
                    em= sq * y + xm;
                } while (em < 0.0);

                em = Math.floor(em);
                t = 0.9 * (1.0 + y*y) * Math.exp(em * alxm - gammln(em + 1.0) - g);
            } while (uniformRV() > t);
        }
        return em;
    }
}




