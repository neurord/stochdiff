package org.textensor.stochdiff.numeric.math;

import java.util.concurrent.BlockingQueue;

import org.textensor.util.inst;

/**
 * Provide caching for a real random generator.
 */
public class CachingRandomGenerator extends MersenneDerived {

    public static final int CAPACITY = 100;

    protected final BlockingQueue<Float> random = inst.newArrayBlockingQueue(CAPACITY);
    protected final BlockingQueue<Double> gaussian = inst.newArrayBlockingQueue(CAPACITY);

    protected final Thread[] fillers = new Thread[2];

    protected static abstract class Filler<V extends Number> implements Runnable {
        protected final RandomGenerator g;
        final BlockingQueue<V> queue;

        Filler(RandomGenerator g, BlockingQueue<V> queue) {
            this.g = g;
            this.queue = queue;
        }

        abstract void copy() throws InterruptedException;

        @Override
        public void run() {
            try {
                this.copy();
            } catch(InterruptedException e) {
            }
        }
    }

    public CachingRandomGenerator(RandomGenerator gen) {
        Filler f;
        f = new Filler(gen.copy(), this.random) {
                @Override
                void copy() throws InterruptedException {
                    while(true)
                        this.queue.put(this.g.random());
                }
            };
        this.fillers[0] = new Thread(f, "random");

        f = new Filler(gen.copy(), this.gaussian) {
                @Override
                void copy() throws InterruptedException {
                    while(true)
                        this.queue.put(this.g.gaussian());
                }
            };
        this.fillers[1] = new Thread(f, "gaussian");

        for(Thread t: this.fillers)
            t.start();
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
    public double gaussian() {
        try {
            return this.gaussian.take();
        } catch(InterruptedException e) {
            return Double.NaN;
        }
    }

    @Override
    public CachingRandomGenerator copy() {
        throw new RuntimeException("not implemented");
    }
}
