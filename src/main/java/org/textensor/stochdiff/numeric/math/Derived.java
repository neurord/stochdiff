package org.textensor.stochdiff.numeric.math;

import static org.textensor.stochdiff.numeric.math.RandomMath.gammln;

/**
 * Part of MersenneTwister which uses random() to generate it's results.
 */
abstract class Derived {

    private static double[] cof = {76.18009173, -86.50532033, 24.01409822,
                                   -1.231739516, 0.120858003e-2, -0.536382e-5
                                  };

    public abstract float random();

    protected boolean haveGaussian;
    protected double spareGaussian;

    public int poisson(double mean) {
        // In "Numerical Recipes" Ch 7-3 p.294
        double em = 0.;
        if (mean < 12.0) {
            double g=Math.exp(-mean);
            em= -1;
            double t=1.0;
            do {
                ++em;
                t *= random();

            } while (t > g);

        } else {
            double sq = Math.sqrt(2.0*mean);
            double alxm=Math.log(mean);
            double g = mean*alxm - gammln(mean+1.0);
            double t = 0.;
            double y = 0.;
            do {
                do {
                    y = Math.tan(Math.PI * random());
                    em = sq*y + mean;
                }  while (em < 0.0);

                em = Math.floor(em);
                t = 0.9*(1.0 + y*y) * Math.exp(em*alxm - gammln(em + 1.0) -g);

            } while (random() > t);
        }

        int ret = (int)(Math.round(em));
        return ret;
    }

    public double gaussian() {
        double ret = 0.;
        if (haveGaussian) {
            ret = spareGaussian;
            haveGaussian = false;
        } else {
            double r = -1;
            double ran1 = 0;
            double ran2 = 0;
            while (r <= 0.0 || r >= 1.0) {
                ran1 = 2 * random() - 1;
                ran2 = 2 * random() - 1;
                r = ran1 * ran1 + ran2 * ran2;
            }
            double fac = Math.sqrt(-2. * Math.log(r) / r);
            ret = ran1 * fac;
            spareGaussian = ran2 * fac;
            haveGaussian = true;
        }
        return ret;
    }

    public void close() {}
}
