package org.textensor.stochdiff.numeric.math;

import java.util.concurrent.BlockingQueue;

import org.textensor.util.inst;

/**
 * Provide caching for a real random generator.
 */
public class CachingRandomGenerator extends MersenneDerived {

    public static final int CAPACITY = 100;

    protected final BlockingQueue<Float> random = inst.newArrayBlockingQueue(CAPACITY);

    protected final Thread filler;

    public CachingRandomGenerator(final RandomGenerator gen) {
        Runnable f = new Runnable() {
                @Override
                public void run() {
                    try {
                        while(true)
                            random.put(gen.random());
                    } catch(InterruptedException e) {
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
