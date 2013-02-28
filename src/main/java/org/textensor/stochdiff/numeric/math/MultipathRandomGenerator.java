package org.textensor.stochdiff.numeric.math;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.util.ResizableArray;
import org.textensor.util.Settings;
import org.textensor.util.inst;

/**
 * Provide caching for a real random generator. Pregenerate
 * numbers from flat and gaussian distributions.
 */
public class MultipathRandomGenerator extends CachingRandomGenerator<Object[]> {
    static final Logger log = LogManager.getLogger(MultipathRandomGenerator.class);

    public static final int DEFAULT_CAPACITY =
        Settings.getProperty("stochdiff.random.size", 128*1024);
    public static final double DEFAULT_FILL = 0.99;

    final int capacity;
    final double fill;
    int deficit = 0, offset = 0;

    protected float[] random = null;
    protected double[] gaussian = null;
    protected double[] spare_gaussian = null;
    protected int[] gaussian_usage = null;

    @Override
    public String toString() {
        return String.format("%s capacity=%d(%f) offset=%d deficit=%d {%s, %s, %s, %s}",
                             getClass().getSimpleName(),
                             capacity, fill, offset, deficit,
                             random.length, gaussian.length, spare_gaussian.length,
                             gaussian_usage.length);
    }

    protected static class StoringGenerator
        extends Derived implements RandomGenerator
    {
        static final Logger log = LogManager.getLogger(StoringGenerator.class);

        protected final RandomGenerator g;
        public final ResizableArray.Float randoms;
        public final ResizableArray.Double gaussians;
        public final ResizableArray.Double spare_gaussians;
        public final ResizableArray.Int gaussian_usage;

        StoringGenerator(RandomGenerator g, int capacity, ResizableArray.Float randoms) {
            this.g = g;

            this.randoms = randoms != null ? randoms : new ResizableArray.Float(capacity);
            this.gaussians = new ResizableArray.Double(capacity);
            this.spare_gaussians = new ResizableArray.Double(capacity);
            this.gaussian_usage = new ResizableArray.Int(capacity);
        }

        @Override
        public float random() {
            final int remaining = this.randoms.remaining();
            float random;
            if (remaining == 0) {
                random = this.g.random();
                this.randoms.put(random);
            }

            random = this.randoms.take(1);
            return random;
        }

        @Override
        public double gaussian() {
            int oldusage = this.randoms.used();
            double gaussian = super.gaussian();
            int newusage = this.randoms.used();
            int used = newusage - oldusage;
            this.gaussians.put(gaussian);
            this.gaussian_usage.put(used);
            double spare = super.gaussian();
            int newnewusage = this.randoms.used();
            assert newnewusage == newusage;
            this.spare_gaussians.put(spare);

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

            Object[] arrays = inst.newArray(store.randoms.asArray(),
                                            store.gaussians.asArray(),
                                            store.spare_gaussians.asArray(),
                                            store.gaussian_usage.asArray());

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
        log.debug("have {} arrays ready", this.queue.size());

        Object[] ar;
        try {
            ar = this.queue.take();
        } catch(InterruptedException e) {
            throw new RuntimeException("unexpected interrupt");
        }

        assert ar.length == 4;

        this.random = (float[]) ar[0];
        this.gaussian = (double[]) ar[1];
        this.spare_gaussian = (double[]) ar[2];
        this.gaussian_usage = (int[]) ar[3];
        this.offset = 0;

        assert this.gaussian.length == this.gaussian_usage.length:
            "gaussian.length=" + this.gaussian.length +
            " g_usage.length=" + this.gaussian_usage.length;

        if (this.deficit > 0) {
            // FIXME: this might fail with overflow exception if the deficit
            // is large enough. A loop would be better, but probably seldom
            // needed.
            log.debug("Reducing deficit of {} from budget {}",
                      this.deficit, this.random.length);
            this.offset += this.deficit;
            this.deficit = 0;
        }
    }

    @Override
    public float random() {
        if (this.random == null || this.offset == this.random.length)
            this._take();

        assert this.random != null;
        assert this.random.length > this.offset:
            "random.length=" + this.random.length + " offset=" + this.offset;
        assert this.gaussian != null;

        return this.random[this.offset++];
    }

    @Override
    public double gaussian() {
        if (this.haveGaussian) {
            this.haveGaussian = false;
            return this.spareGaussian;
        }

        if (this.random == null || this.offset == this.random.length)
            this._take();

        assert this.random != null;
        assert this.random.length > this.offset:
            "random.length=" + this.random.length + " offset=" + this.offset;
        assert this.gaussian != null;

        int wanted = this.gaussian_usage[this.offset];
        int usage = Math.min(this.random.length - this.offset, wanted);
        if (usage < wanted)
            this.deficit = wanted - usage;

        double ans = this.gaussian[this.offset];
        this.haveGaussian = true;
        this.spareGaussian = this.spare_gaussian[this.offset];
        this.offset += usage;
        return ans;
    }
}
