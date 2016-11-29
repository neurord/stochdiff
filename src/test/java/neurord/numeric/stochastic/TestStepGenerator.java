package neurord.numeric.stochastic;

import java.util.Random;
import java.util.ArrayList;

import neurord.numeric.math.MersenneTwister;
import neurord.numeric.BaseCalc.distribution_t;

import static org.testng.Assert.assertEquals;
import neurord.util.TestUtil;
import static neurord.util.TestUtil.assertArrayEquals;
import static neurord.util.TestUtil.assertApproxEquals;
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
            new Object[] { 10., r.nextLong()},
            new Object[] { 30., r.nextLong()},
            new Object[] { 100., r.nextLong()},
            new Object[] { 300., r.nextLong()},
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

    @Test(dataProvider="means", threadPoolSize = 4, invocationCount = 5, enabled = false)
    public static void gaussianStep(double mean, long seed) {
        int nsp = 0;
        final MersenneTwister random = new MersenneTwister(seed);
        final int ntrials = 100000;

        final int ntot = 1000;

        final double p = mean / ntot;

        final StepGenerator stepper = new StepGenerator(random);
        for (int i = 0; i < ntrials; i++)
            nsp += stepper.versatile_ngo(ntot, p);

        double sigma = Math.sqrt(mean / ntrials * (ntrials - mean) / ntrials * ntrials);
        log.info("versatile_ngo: mean={} σ={} expect={} actual={}({}σ)",
                 mean, sigma, ntrials * mean, nsp, (ntrials*mean - nsp) / sigma);
        assertApproxEquals(nsp, ntrials * mean, 0.01, Math.max(10 * sigma, 1));
    }

    private static double[] rs(Random gen, int count) {
        double[] a = new double[count];
        for (int i = 0; i < count; i++)
            a[i] = gen.nextDouble();
        return a;
    }
}
