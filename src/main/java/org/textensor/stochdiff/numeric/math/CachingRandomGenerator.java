package org.textensor.stochdiff.numeric.math;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.concurrent.BlockingQueue;

import org.textensor.util.inst;

/**
 * Provide caching for a real random generator.
 */
public class CachingRandomGenerator extends MersenneDerived {
    static final Logger log = LogManager.getLogger(CachingRandomGenerator.class);

    public static final int NRANDOMS = 4;
    public static final int CAPACITY = 16*1024*1024;

    protected final BlockingQueue<float[]> randoms
        = inst.newArrayBlockingQueue(NRANDOMS);
    protected float[] random = null;
    protected int remaining = 0;

    protected final Thread filler;

    public CachingRandomGenerator(final RandomGenerator gen) {
        Runnable f = new Runnable() {
                @Override
                public void run() {

                    int count = 0;
                    while(true) {
                        float[] produce = new float[CAPACITY];
                        for (int i = 0; i < produce.length; i++)
                            produce[i] = gen.random();
                        if(count++ % 1 == 0)
                            log.info("random queue has {} arrays",
                                     randoms.size());
                        try {
                            randoms.put(produce);
                        } catch(InterruptedException e) {
                            log.info("filler thread interrupted, wrapping up");
                        }
                    }
                }
            };
        this.filler = new Thread(f, "random");
        this.filler.start();
    }

    protected void finalize() {
        this.close();
    }

    @Override
    public float random() {
        if (this.remaining == 0) {
            try {
                this.random = this.randoms.take();
                this.remaining = this.random.length;
            } catch(InterruptedException e) {
                throw new RuntimeException("unexpected interrupt");
            }
        }

        assert this.random != null;

        float ans = this.random[this.random.length - this.remaining];
        this.remaining--;
        return ans;
    }

    @Override
    public CachingRandomGenerator copy() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public void close() {
        this.filler.stop();
    }
}
