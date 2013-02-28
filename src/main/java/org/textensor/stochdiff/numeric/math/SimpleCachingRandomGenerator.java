package org.textensor.stochdiff.numeric.math;

import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.util.Settings;

public class SimpleCachingRandomGenerator
    extends CachingRandomGenerator<float[]>
{
    static final Logger log =
        LogManager.getLogger(SimpleCachingRandomGenerator.class);

    public static final int DEFAULT_CAPACITY =
        Settings.getProperty("stochdiff.random.size", 128*1024);
    final int capacity;

    protected float[] random = null;
    protected int remaining = 0;

    protected class Filler extends CachingRandomGenerator.Filler {
        public Filler(RandomGenerator gen) {
            super(gen);
        }

        @Override
        public boolean _run() {
            float[] produce = new float[capacity];
            for (int i = 0; i < produce.length; i++)
                produce[i] = gen.random();
            try {
                queue.put(produce);
            } catch(InterruptedException e) {
                log.info("filler thread interrupted, wrapping up");
                return false;
            } catch(IllegalMonitorStateException e) {
                log.warn("filler thread errored out, wrapping up", e);
                return false;
            }
            return true;
        }
    }

    @Override
    protected Filler newFiller(RandomGenerator gen) {
        return new Filler(gen);
    }

    public SimpleCachingRandomGenerator(RandomGenerator gen, int capacity) {
        super(gen);
        this.capacity = capacity;
    }

    public SimpleCachingRandomGenerator(RandomGenerator gen) {
        this(gen, DEFAULT_CAPACITY);
    }

    @Override
    public float random() {
        if (this.remaining == 0) {
            log.debug("have {} arrays ready", this.queue.size());
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
