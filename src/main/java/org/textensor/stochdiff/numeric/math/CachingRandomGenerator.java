package org.textensor.stochdiff.numeric.math;

import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.util.inst;

/**
 * Provide caching for a real random generator.
 */
public class CachingRandomGenerator extends MersenneDerived {
    static final Logger log = LogManager.getLogger(CachingRandomGenerator.class);

    public static final int CAPACITY = 1073741824;

    protected final BlockingQueue<Float> random = inst.newArrayBlockingQueue(CAPACITY);

    protected final Thread filler;

    public CachingRandomGenerator(final RandomGenerator gen) {
        Runnable f = new Runnable() {
                @Override
                public void run() {
                    int i = 0;
                    try {
                        while(true) {
                            random.put(gen.random());
                            if(i++ % 1048576 == 0)
                                log.info("random queue size is {}", random.size());
                        }
                    } catch(InterruptedException e) {
                        log.info("filler interrupted");
                    }
                }
            };
        this.filler = new Thread(f, "random");
        this.filler.start();
    }

    @Override
    public float random() {
        try {
            return this.random.take();
        } catch(InterruptedException e) {
            return Float.NaN;
        }
    }

    @Override
    public CachingRandomGenerator copy() {
        throw new RuntimeException("not implemented");
    }
}
