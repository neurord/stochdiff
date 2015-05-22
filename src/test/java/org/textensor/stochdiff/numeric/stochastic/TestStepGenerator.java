package org.textensor.stochdiff.numeric.stochastic;

import java.util.Random;
import java.util.ArrayList;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.stochdiff.numeric.BaseCalc.distribution_t;
import org.textensor.util.inst;

import static org.testng.Assert.assertEquals;
import org.textensor.util.TestUtil;
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

        final StepGenerator stepper = new InterpolatingStepGenerator(distribution_t.BINOMIAL, random);
        for (int i = 0; i < ntrials; i++)
            nsp += stepper.gaussianStep(ntot, p);

        double sigma = Math.sqrt(mean * (ntrials - mean) / ntrials) * Math.sqrt(ntrials);
        log.info("gaussian step: mean={} σ={} expect={} actual={}({}σ)",
                 mean, sigma, ntrials * mean, nsp, (ntrials*mean - nsp) / sigma);
        assertApproxEquals(nsp, ntrials * mean, 0, Math.max(10 * sigma, 1));
    }

    @DataProvider
    public static Object[][] simple() {
        return TestUtil.multiply
            (   new Object[]{ new InterpolatingStepGenerator(distribution_t.BINOMIAL, new MersenneTwister(0)),
                              new InterpolatingStepGenerator(distribution_t.POISSON, new MersenneTwister(0)) },
                new Object[]{
                    new Object[]{ 1, 0., 0., 0 }, // fails now because only n>=2 is supported
                    new Object[]{ 2, 0., 0., 0 },
                    new Object[]{ 2, 0., 0.5, 0 },
                    new Object[]{ 2, 0., 0., 0 },
                    new Object[]{ 2, 0., 1., 0 },
                });
    }

    @Test(dataProvider="simple")
    public static void interpolated_nGo(InterpolatingStepGenerator gen,
                                        int n, double p, double r, int expected) {
        assertEquals(gen.nGo(n, Math.log(p), r), expected);
    }

    private static double[] rs(Random gen, int count) {
        double[] a = new double[count];
        for (int i = 0; i < count; i++)
            a[i] = gen.nextDouble();
        return a;
    }

    @DataProvider
    public static Object[][] randomized() {
        Random r = new Random();

        return TestUtil.multiply
            (   new Object[]{ new InterpolatingStepGenerator(distribution_t.BINOMIAL, new MersenneTwister(0)),
                              new InterpolatingStepGenerator(distribution_t.POISSON, new MersenneTwister(0)) },
                new Object[]{   /* n, lnp, rs, expected */
                    new Object[]{ 2 + r.nextInt(120), Float.NEGATIVE_INFINITY,
                                  rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -20., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -19., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -18., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -17., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -16., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -15., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -14., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -13., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -12., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -11., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -10., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -9., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -8., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -7., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -6., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -5., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -4., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -3., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -2., rs(r, 1000000), 0 },
                    new Object[]{ 2 + r.nextInt(120), -1., rs(r, 1000000), 0 },
                });
    }

    @Test(dataProvider="randomized")
    public static void mean_nGo(InterpolatingStepGenerator gen,
                                int n, double lnp, double[] rs, int expected) {
        long ngo = 0;
        double p = Math.exp(lnp);
        for (double r: rs)
            ngo += gen.nGo(n, lnp, r);
        double sigma = Math.sqrt(n * p * (1-p)) * Math.sqrt(rs.length);
        double expect = n * p * rs.length;
        log.info("interpolated nGo: p={} σ={} expect={} actual={}({}σ)",
                 p, sigma, expect, ngo, (expect - ngo) / sigma);
        assertApproxEquals(ngo, expect, 0, Math.max(10 * sigma, 1));
    }
}
