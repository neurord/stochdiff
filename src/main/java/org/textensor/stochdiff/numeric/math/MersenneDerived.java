package org.textensor.stochdiff.numeric.math;

/**
 * Part of MersenneTwister which uses random() to generate it's results.
 */
abstract class MersenneDerived implements RandomGenerator {

    private static double[] cof = {76.18009173, -86.50532033, 24.01409822,
                                   -1.231739516, 0.120858003e-2, -0.536382e-5
                                  };

    @Override
    public abstract float random();

    @Override
    public final double gammln(double xx) {
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

    protected boolean haveGaussian;
    protected double spareGaussian;

    @Override
    public final int poisson(double mean) {
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

    @Override
    public final double gaussian() {
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

    @Override
    public abstract RandomGenerator copy();

    @Override
    public void close() {}
}
