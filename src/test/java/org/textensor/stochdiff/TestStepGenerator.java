package org.textensor.stochdiff;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.stochastic.StepGenerator;

import static org.testng.Assert.assertEquals;
import static org.textensor.util.TestUtil.assertArrayEquals;
import static org.textensor.util.TestUtil.assertApproxEquals;
import org.testng.annotations.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class TestStepGenerator {
    static final Logger log = LogManager.getLogger(TestStepGenerator.class);

    @DataProvider
    public Object[][] means() {
        return new Object[][] {
            new Object[] { 1.e-7},
            new Object[] { 1.e-6},
            new Object[] { 1.e-5},
            new Object[] { 1.e-4},
            new Object[] { 1.e-3},
            new Object[] { 1.e-2},
            new Object[] { 1.e-1},
            new Object[] { 0.5},
            new Object[] { 0.9},
            new Object[] { 1.1},
            new Object[] { 3.},
            new Object[] { 10},
            new Object[] { 30},
            new Object[] { 100},
        };
    };

    @Test(dataProvider="means")
    public static void poisson(double mean) {
        int nsp = 0;
        int nrp = 0;
        final MersenneTwister random = new MersenneTwister();
        final int ntrials = 100000;

        for (int i = 0; i < ntrials; i++)
            nrp += random.poisson(mean);

        double sigma = Math.sqrt(mean) * Math.sqrt(ntrials);
        log.info("poisson: nevents mean={} expect={} nrp={} σ={}",
                 mean, ntrials * mean, nrp, sigma);
        assertApproxEquals(nrp, ntrials * mean, 0, Math.max(10 * sigma, 1));
    }

    @Test(dataProvider="means")
    public static void gaussianStep(double mean) {
        int nsp = 0;
        final MersenneTwister random = new MersenneTwister();
        final int ntrials = 100000;

        final int ntot = 1000;

        final double p = mean / ntot;

        for (int i = 0; i < ntrials; i++)
            nsp += StepGenerator.gaussianStep(ntot, p, random.gaussian(), random.random());

        double sigma = Math.sqrt(mean * (ntrials - mean) / ntrials) * Math.sqrt(ntrials);
        log.info("gaussian step: nevents mean={} expect={} nsp={} σ={}",
                 mean, ntrials * mean, nsp, sigma);
        assertApproxEquals(nsp, ntrials * mean, 0, Math.max(10 * sigma, 1));
    }
}
