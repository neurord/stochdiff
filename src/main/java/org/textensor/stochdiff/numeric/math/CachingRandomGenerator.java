package org.textensor.stochdiff.numeric.math;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.util.inst;
import org.textensor.util.SynchronizedFloatArray;

/**
 * Provide caching for a real random generator.
 */
public class CachingRandomGenerator extends MersenneDerived {
    static final Logger log = LogManager.getLogger(CachingRandomGenerator.class);

    public static final int CAPACITY = 16*1024;

    protected final SynchronizedFloatArray random =
        new SynchronizedFloatArray(CAPACITY);

    protected final Thread filler;

    public CachingRandomGenerator(final RandomGenerator gen) {
        Runnable f = new Runnable() {
                @Override
                public void run() {
                    int i = 0;
                    while(true) {
                        random.put(gen.random());
                        if(i++ % 1048576 == 0)
                            log.info("random queue size is {}", random.size());
                    }
                }
            };
        this.filler = new Thread(f, "random");
        this.filler.start();
    }

    @Override
    public float random() {
        return this.random.take();
    }

    @Override
    public CachingRandomGenerator copy() {
        throw new RuntimeException("not implemented");
    }
}
