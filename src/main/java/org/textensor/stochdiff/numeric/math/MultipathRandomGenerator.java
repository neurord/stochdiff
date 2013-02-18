package org.textensor.stochdiff.numeric.math;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.util.ResizableArray;

/**
 * Provide caching for a real random generator. Pregenerate
 * numbers from flat and gaussian distributions.
 */
public class MultipathRandomGenerator extends CachingRandomGenerator<ResizableArray[]> {
    static final Logger log = LogManager.getLogger(MultipathRandomGenerator.class);

    public static final int CAPACITY = 16*1024*1024;

    protected ResizableArray.Float random = null;
    protected ResizableArray.Double gaussian = null;
    protected ResizableArray.Int gaussian_usage = null;

    public static final int FILL = (int) (CAPACITY * 0.9);

    protected static class StoringGenerator
        extends Derived implements RandomGenerator
    {
        protected final RandomGenerator g;
        public final ResizableArray.Float randoms;
        public final ResizableArray.Double gaussians;
        public final ResizableArray.Int gaussian_usage;

        StoringGenerator(RandomGenerator g, int capacity) {
            this.g = g;

            this.randoms = new ResizableArray.Float(capacity);
            this.gaussians = new ResizableArray.Double(capacity);
            this.gaussian_usage = new ResizableArray.Int(capacity);
        }

        @Override
        public float random() {
            if (this.randoms.remaining() > 0)
                return this.randoms.take();

            float random = this.g.random();
            this.randoms.put(random);
            return random;
        }

        @Override
        public double gaussian() {
            int size = this.randoms.size();
            double gaussian = super.gaussian();
            int used = this.randoms.size() - size;
            this.gaussians.put(gaussian);
            this.gaussian_usage.put(used);
            this.randoms.move(-used + 1);
            return gaussian;
        }

        public void step() {
            this.gaussian();
        }

        @Override
        public int poisson(double mean) {
            throw new RuntimeException("do not use");
        }
    }

    protected class Filler extends CachingRandomGenerator.Filler {
        public Filler(RandomGenerator gen) {
            super(gen);
        }

        @Override
        public boolean _run() {
            StoringGenerator store = new StoringGenerator(this.gen, CAPACITY);

            for (int i = 0; store.randoms.size() < FILL; i++)
                store.step();

            ResizableArray[] ar = new ResizableArray[]
                {store.randoms, store.gaussians, store.gaussian_usage};
            try {
                MultipathRandomGenerator.this.queue.put(ar);
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

    public MultipathRandomGenerator(final RandomGenerator gen) {
        super(gen);
    }

    public void _take() {
        ResizableArray[] ar;
        try {
            ar = this.queue.take();
        } catch(InterruptedException e) {
            throw new RuntimeException("unexpected interrupt");
        }

        this.random = (ResizableArray.Float) ar[0];
        this.gaussian = (ResizableArray.Double) ar[1];
        this.gaussian_usage = (ResizableArray.Int) ar[2];

        assert this.gaussian.size() == this.gaussian_usage.size();
        assert this.random.size() == this.gaussian.size()
            + this.gaussian_usage.get(this.gaussian.size()-1);
    }

    @Override
    public float random() {
        if (this.random == null || this.random.remaining() == 0)
            this._take();

        assert this.random != null;

        this.gaussian.move(+1);
        this.gaussian_usage.move(+1);
        return this.random.take();
    }

    @Override
    public double gaussian() {
        if (this.random == null || this.random.remaining() == 0)
            this._take();

        assert this.random != null;

        int usage = this.gaussian_usage.take(0);
        this.gaussian_usage.move(usage);
        this.random.move(usage);
        return this.gaussian.take(usage);
    }
}
