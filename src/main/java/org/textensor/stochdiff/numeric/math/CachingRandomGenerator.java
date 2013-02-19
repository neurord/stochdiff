package org.textensor.stochdiff.numeric.math;

import java.util.concurrent.BlockingQueue;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.util.inst;

/**
 * Provide caching for a real random generator.
 */
public abstract class CachingRandomGenerator<T>
    extends Derived implements RandomGenerator
{
    static final Logger log = LogManager.getLogger(CachingRandomGenerator.class);

    public static final int NRANDOMS = 4;
    protected final BlockingQueue<T> queue
        = inst.newArrayBlockingQueue(NRANDOMS);

    protected final Thread filler;

    protected abstract class Filler implements Runnable {
        protected final RandomGenerator gen;
        public Filler(RandomGenerator gen) {
            this.gen = gen;
        }

        @Override
        public void run() {
            int count = 0;
            while(this._run()) {
                if (count++ % 1 == 0)
                    log.info("{}: random queue has {} arrays",
                             CachingRandomGenerator.this.getClass().getSimpleName(),
                             queue.size());
            }
        }

        protected abstract boolean _run();
    }

    abstract protected Filler newFiller(RandomGenerator gen);

    public CachingRandomGenerator(final RandomGenerator gen) {
        Runnable f = this.newFiller(gen);
        this.filler = new Thread(f, "random");
        this.filler.start();
    }

    protected void finalize() {
        this.close();
    }

    @Override
    public void close() {
        this.filler.stop();
        super.close();
    }

    public static RandomGenerator newRandomGenerator(long seed) {
        final String generator = System.getProperty("stochdiff.random", "mersenne");

        switch(generator) {
        case "mersenne":
            return new MersenneTwister(seed);
        case "simplecaching":
            return new SimpleCachingRandomGenerator(new MersenneTwister(seed));
        case "multicaching":
            return new MultipathRandomGenerator(new MersenneTwister(seed));
        default:
            throw new RuntimeException("unknown random generator: " + generator);
        }
    }
}
