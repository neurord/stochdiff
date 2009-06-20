package org.textensor.stochdiff;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.stochastic.StepGenerator;





public class StepgenTests {



    public static void main(String[] argv) {
        runtests();
    }



    public static void runtests() {
        int nsp = 0;
        int nrp = 0;
        MersenneTwister random = new MersenneTwister(12345);
        int ntrials = 100000;

        int ntot = 1000;
        double[] means = {1.e-7, 1.e-6, 1.e-5, 1.e-4, 1.e-3, 1.e-2, 1.e-1, 0.9, 1.1, 3., 10};

        for (int j = 0; j < means.length; j++) {
            nsp = 0;
            nrp = 0;
            double p = 0.999 * means[j] / ntot;
            for (int i = 0; i < ntrials; i++) {
                nrp += random.poisson(means[j]);
                nsp += StepGenerator.gaussianStep(ntot, p, random.gaussian(), random.random());
            }
            E.info("nevents mean=" + means[j] + " expect=" + (ntrials * means[j]) + " " + nrp + " " + nsp);
        }

    }



}