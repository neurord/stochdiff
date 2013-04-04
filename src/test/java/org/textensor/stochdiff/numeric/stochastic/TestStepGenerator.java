package org.textensor.stochdiff.numeric.stochastic;

import java.util.Random;
import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.BaseCalc.distribution_t;
import org.textensor.util.inst;

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
        Random r = new Random();
        return new Object[][] {
            new Object[] { 1.e-7, r.nextLong()},
            new Object[] { 1.e-6, r.nextLong()},
            new Object[] { 1.e-5, r.nextLong()},
            new Object[] { 1.e-4, r.nextLong()},
            new Object[] { 1.e-3, r.nextLong()},
            new Object[] { 1.e-2, r.nextLong()},
            new Object[] { 1.e-1, r.nextLong()},
            new Object[] { 0.5, r.nextLong()},
            new Object[] { 0.9, r.nextLong()},
            new Object[] { 1.1, r.nextLong()},
            new Object[] { 3., r.nextLong()},
            new Object[] { 10, r.nextLong()},
            new Object[] { 30, r.nextLong()},
            new Object[] { 100, r.nextLong()},
            new Object[] { 300, r.nextLong()},
        };
    };

    @Test(dataProvider="means")
    public static void poisson(double mean, long seed) {
        int nsp = 0;
        int nrp = 0;
        final MersenneTwister random = new MersenneTwister(seed);
        final int ntrials = 100000;

        for (int i = 0; i < ntrials; i++)
            nrp += random.poisson(mean);

        double sigma = Math.sqrt(mean) * Math.sqrt(ntrials);
        log.info("poisson: mean={} σ={} expect={} actual={}({}σ)",
                 mean, sigma, ntrials * mean, nrp, (ntrials * mean - nrp)/sigma);
        assertApproxEquals(nrp, ntrials * mean, 0, Math.max(10 * sigma, 1));
    }

    @Test(dataProvider="means", threadPoolSize = 4, invocationCount = 5)
    public static void gaussianStep(double mean, long seed) {
        int nsp = 0;
        final MersenneTwister random = new MersenneTwister(seed);
        final int ntrials = 100000;

        final int ntot = 1000;

        final double p = mean / ntot;

        for (int i = 0; i < ntrials; i++)
            nsp += StepGenerator.gaussianStep(ntot, p, random.gaussian(), random.random());

        double sigma = Math.sqrt(mean * (ntrials - mean) / ntrials) * Math.sqrt(ntrials);
        log.info("gaussian step: mean={} σ={} expect={} actual={}({}σ)",
                 mean, sigma, ntrials * mean, nsp, (ntrials*mean - nsp) / sigma);
        assertApproxEquals(nsp, ntrials * mean, 0, Math.max(10 * sigma, 1));
    }
}
