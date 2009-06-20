// CCWS-LICENSE

package org.catacomb.numeric.function;

import org.catacomb.numeric.math.Random;




public final class AR1Noise {

    double regression;
    double amplitude;
    double mean;

    double x;

    double fnew;

    public AR1Noise() {
        mean = 0.;
        amplitude = 1;
        setRegression(0.95);
    }


    public void setRegression(double d) {
        regression = (d < 0. ? 0. : (d > 1. ? 1. : d));
        fnew = Math.sqrt(1. - regression * regression);
    }

    public void setAmplitude(double d) {
        amplitude = d;
    }

    public void setMean(double d) {
        mean = d;
    }


    // this is wrong since it is call dependent... ***;
    public double nextValue() {
        x = regression * x + fnew * amplitude * Random.gaussianRV();
        return mean + x;
    }

    public static double nextValue(double cvalue, double amp, double reg) {
        double ff = Math.sqrt(1. - reg * reg);
        double ret = reg * cvalue + ff * amp * Random.gaussianRV();
        return ret;
    }


}







