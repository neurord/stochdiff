package org.textensor.stochdiff.numeric.math;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.util.ResizableArray;
import org.textensor.util.inst;

/**
 * Provide caching for a real random generator. Pregenerate
 * numbers from flat and gaussian distributions.
 */
public class MultipathRandomGenerator extends CachingRandomGenerator<ResizableArray[]> {
    static final Logger log = LogManager.getLogger(MultipathRandomGenerator.class);

    public static final int DEFAULT_CAPACITY = 16*1024*1024;
    public static final double DEFAULT_FILL = 0.9;

    final int capacity;
    final double fill;
    int deficit = 0;

    protected ResizableArray.Float random = null;
    protected ResizableArray.Double gaussian = null;
    protected ResizableArray.Int gaussian_usage = null;

    @Override
    public String toString() {
        return String.format("%s capacity=%d fill=%f deficit=%d {%s, %s, %s}",
                             getClass().getSimpleName(),
                             capacity, fill, deficit,
                             random, gaussian, gaussian_usage);
    }

    protected static class StoringGenerator
        extends Derived implements RandomGenerator
    {
        protected final RandomGenerator g;
        public final ResizableArray.Float randoms;
        public final ResizableArray.Double gaussians;
        public final ResizableArray.Int gaussian_usage;

        StoringGenerator(RandomGenerator g, int capacity, ResizableArray.Float randoms) {
            this.g = g;

            this.randoms = randoms != null ? randoms : new ResizableArray.Float(capacity);
            this.gaussians = new ResizableArray.Double(capacity);
            this.gaussian_usage = new ResizableArray.Int(capacity);
        }

        @Override
        public float random() {
            final int remaining = this.randoms.remaining();
            if (remaining == 0) {
                float random = this.g.random();
                this.randoms.put(random);
                MultipathRandomGenerator.log.debug("creating (remaining={} -> {})",
                                                   remaining, this.randoms.remaining());
            }

            MultipathRandomGenerator.log.debug("reusing (remaining={})", remaining);
            return this.randoms.take();
        }

        @Override
        public double gaussian() {
            int oldusage = this.randoms.used();
            double gaussian = super.gaussian();
            int newusage = this.randoms.used();
            int used = newusage - oldusage;
            this.gaussians.put(gaussian);
            this.gaussian_usage.put(used);
            MultipathRandomGenerator.log.debug("gaussian oldusage={} newusage={}, moving {}",
                                               oldusage, newusage, -used+1);
            this.randoms.take(-used+1);
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
        ResizableArray.Float overflow = null;

        public Filler(RandomGenerator gen) {
            super(gen);
        }

        @Override
        public boolean _run() {
            StoringGenerator store = new StoringGenerator(this.gen, capacity, this.overflow);

            for (int i = 0; store.randoms.size() < fill * capacity; i++)
                store.step();

            assert store.randoms.size() >= store.gaussians.size();
            this.overflow = store.randoms.cut(store.gaussians.size(), capacity);

            ResizableArray[] arrays =
                inst.newArray(store.randoms, store.gaussians, store.gaussian_usage);
            for(ResizableArray array: arrays)
                array.reset();

            try {
                MultipathRandomGenerator.this.queue.put(arrays);
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

    public MultipathRandomGenerator(RandomGenerator gen, int capacity, double fill) {
        super(gen);
        this.capacity = capacity;
        this.fill = fill;
    }

    public MultipathRandomGenerator(RandomGenerator gen) {
        this(gen, DEFAULT_CAPACITY, DEFAULT_FILL);
    }

    public void _take() {
        log.info("have {} arrays ready", this.queue.size());

        ResizableArray[] ar;
        try {
            ar = this.queue.take();
        } catch(InterruptedException e) {
            throw new RuntimeException("unexpected interrupt");
        }

        this.random = (ResizableArray.Float) ar[0];
        this.gaussian = (ResizableArray.Double) ar[1];
        this.gaussian_usage = (ResizableArray.Int) ar[2];

        assert this.gaussian.size() == this.gaussian_usage.size():
            "gaussian.size()=" + this.gaussian.size() +
            " g_usage.size()=" + this.gaussian_usage.size();

        if (this.deficit > 0) {
            // FIXME: this might fail with overflow exception if the deficit
            // is large enough. A loop would be better, but probably seldom
            // needed.
            this.random.take(this.deficit);
            this.gaussian.take(this.deficit);
            this.gaussian_usage.take(this.deficit);
            log.info("Reducing deficit of {} from budget {}",
                      this.deficit, this.random.size());
            this.deficit = 0;
        }
    }

    @Override
    public float random() {
        if (this.random == null || this.random.remaining() == 0) {
            this._take();
            assert this.random.remaining() == this.random.size();
            assert this.gaussian.remaining() == this.gaussian.size();
        }

        assert this.random != null;
        assert this.random.remaining() > 0:
            "random.r=" + this.random.remaining() + " gaussian.r=" + this.gaussian.remaining();
        assert this.gaussian != null;
        assert this.gaussian.remaining() > 0:
            "random.r=" + this.random.remaining() + " gaussian.r=" + this.gaussian.remaining();

        this.gaussian.take(1);
        this.gaussian_usage.take(1);
        return this.random.take(1);
    }

    @Override
    public double gaussian() {
        if (this.random == null || this.random.remaining() == 0) {
            this._take();
            assert this.random.remaining() <= this.random.size();
            assert this.gaussian.remaining() <= this.gaussian.size();
        }

        assert this.random != null;
        assert this.random.remaining() > 0:
            "random.r=" + this.random.remaining() + " gaussian.r=" + this.gaussian.remaining();
        assert this.gaussian != null;
        assert this.gaussian.remaining() > 0:
            "random.r=" + this.random.remaining() + " gaussian.r=" + this.gaussian.remaining();

        int wanted = this.gaussian_usage.take(0);
        int usage = Math.min(this.random.remaining(), wanted);
        if (usage < wanted)
            this.deficit = wanted - usage;
        this.gaussian_usage.take(usage);
        this.random.take(usage);
        return this.gaussian.take(usage);
    }
}
