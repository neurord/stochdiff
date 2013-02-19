package org.textensor.stochdiff.numeric.math;

import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static org.testng.Assert.assertEquals;
import org.testng.annotations.*;

public class TestRandomGenerators {
    static final Logger log = LogManager.getLogger(TestRandomGenerators.class);

    final long seed = 0xDEADBEEF;

    @DataProvider(name="generators")
    public Object[][] createData() {
        return new Object[][] {
            new Object[] {new MersenneTwister(seed)},
            new Object[] {new SimpleCachingRandomGenerator(new MersenneTwister(seed), 1024)},
            new Object[] {new MultipathRandomGenerator(new MersenneTwister(seed), 1024, 0.9)},
        };
    }

    @Test(dataProvider="generators")
    public void testRunningRandom(RandomGenerator g)
    {
        log.debug("running testRunningRandom with {}", g);
        int length = 1024 * 3;
        for (int i = 0; i < length; i++)
            g.random();
    }

    @Test(dataProvider="generators")
    public void testRunningGaussian(RandomGenerator g)
    {
        log.debug("running testRunningGaussian with {}", g);
        int length = 1024;
        for (int i = 0; i < length; i++) {
            log.debug("g={}", g);
            g.gaussian();
        }
    }

    @Test(dataProvider="generators")
    public void testRunningPoisson(RandomGenerator g)
    {
        log.debug("running testRunningPoisson with {}", g);
        int length = 1024;
        for (int i = 0; i < length; i++)
            g.poisson((double)i);
    }

    @Test(dependsOnMethods={"testRunningRandom",
                            "testRunningGaussian",
                            "testRunningPoisson"})
    public void testSameness() {
        log.info("running testSameness with {} generators", 4);

        final RandomGenerator generators[] = {
            new MersenneTwister(seed),
            new MersenneTwister(seed),
            new SimpleCachingRandomGenerator(new MersenneTwister(seed), 128),
            new MultipathRandomGenerator(new MersenneTwister(seed), 128, 0.9),
        };

        int length = 1024;

        Random r = new Random(1234);

        for (int i = 0; i < length; i++) {
            switch(r.nextInt(1)) {
            case 0: {
                float[] vals = new float[generators.length];
                for(int j = 0; j < vals.length; j++) {
                    log.debug("i={} j={} using random()", i, j);
                    vals[j] = generators[j].random();
                    if (j > 0)
                        assertEquals(vals[j], vals[0],
                                     "" + vals[0] + ", " + vals[j]);
                }
            }; break;

            case 1: {
                double[] vals = new double[generators.length];
                for(int j = 0; j < vals.length; j++) {
                    log.debug("i={} j={} using gaussian()", i, j);
                    vals[j] = generators[j].gaussian();
                    if (j > 0)
                        assertEquals(vals[j], vals[0],
                                     "" + vals[0] + ", " + vals[j]);
                }
            }; break;

            case 2: {
                int[] vals = new int[generators.length];
                for(int j = 0; j < vals.length; j++) {
                    log.debug("i={} j={} using poisson()", i, j);
                    vals[j] = generators[j].poisson(r.nextDouble());
                    if (j > 0)
                        assertEquals(vals[j], vals[0],
                                     "" + vals[0] + ", " + vals[j]);
                }
            }; break;

            default:
                assert false;
            }
        }

        log.info("closign generators", 4);

        for(RandomGenerator g: generators)
            g.close();
    }
}
