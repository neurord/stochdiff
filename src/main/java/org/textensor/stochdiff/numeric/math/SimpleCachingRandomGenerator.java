package org.textensor.stochdiff.numeric.math;

import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class SimpleCachingRandomGenerator
    extends CachingRandomGenerator<float[]>
{
    static final Logger log =
        LogManager.getLogger(SimpleCachingRandomGenerator.class);

    public static final int CAPACITY = 16*1024*1024;

    protected float[] random = null;

    protected class Filler extends CachingRandomGenerator.Filler {
        public Filler(RandomGenerator gen) {
            super(gen);
        }

        @Override
        public boolean _run() {
            float[] produce = new float[CAPACITY];
            for (int i = 0; i < produce.length; i++)
                produce[i] = gen.random();
            try {
                queue.put(produce);
            } catch(InterruptedException e) {
                log.info("filler thread interrupted, wrapping up");
                return false;
            }
            return true;
        }
    }

    @Override
    protected Filler newFiller(RandomGenerator gen) {
        return new Filler(gen);
    }

    public SimpleCachingRandomGenerator(final RandomGenerator gen) {
        super(gen);
    }

    @Override
    public float random() {
        if (this.remaining == 0) {
            try {
                this.random = this.queue.take();
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
}
