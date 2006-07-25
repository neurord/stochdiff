package org.textensor.stochdiff.numeric.math;


public class RandomMath {


    private static double[] cof = {76.18009173, -86.50532033, 24.01409822,
                                   -1.231739516, 0.120858003e-2, -0.536382e-5
                                  };



    public final static int poissonInt(double d, MersenneTwister gen) {
        return (int)(poidev(d, gen));
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



    public final static double poidev(double xm, MersenneTwister gen) {

        double em = -1.;
        if (xm < 12.0) {
            double g = Math.exp(-xm);
            double t = 1.0;
            do {
                em += 1.0;
                t *= gen.random();
            } while (t > g);


        } else {
            double sq = Math.sqrt(2.0 * xm);
            double alxm = Math.log(xm);
            double g = xm * alxm - gammln(xm + 1.0);
            double y = 0.;
            double t = -1.;
            do {
                do {
                    y = Math.tan(Math.PI*gen.random());
                    em= sq * y + xm;
                } while (em < 0.0);

                em = Math.floor(em);
                t = 0.9 * (1.0 + y*y) * Math.exp(em * alxm - gammln(em + 1.0) - g);
            } while (gen.random() > t);
        }
        return em;
    }
}
