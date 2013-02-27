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
    protected ResizableArray.Double spare_gaussian = null;

    @Override
    public String toString() {
        return String.format("%s capacity=%d fill=%f deficit=%d {%s, %s, %s, %s}",
                             getClass().getSimpleName(),
                             capacity, fill, deficit,
                             random, gaussian, gaussian_usage, spare_gaussian);
    }

    protected static class StoringGenerator
        extends Derived implements RandomGenerator
    {
        protected final RandomGenerator g;
        public final ResizableArray.Float randoms;
        public final ResizableArray.Double gaussians;
        public final ResizableArray.Int gaussian_usage;
        public final ResizableArray.Double spare_gaussians;

        StoringGenerator(RandomGenerator g, int capacity, ResizableArray.Float randoms) {
            this.g = g;

            this.randoms = randoms != null ? randoms : new ResizableArray.Float(capacity);
            this.gaussians = new ResizableArray.Double(capacity);
            this.gaussian_usage = new ResizableArray.Int(capacity);
            this.spare_gaussians = new ResizableArray.Double(capacity);
        }

        @Override
        public float random() {
            final int remaining = this.randoms.remaining();
            float random;
            if (remaining == 0) {
                random = this.g.random();
                this.randoms.put(random);
            }

            random = this.randoms.take();
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

            ResizableArray[] arrays =
                inst.newArray(store.randoms,
                              store.gaussians, store.gaussian_usage, store.spare_gaussians);
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
        log.debug("have {} arrays ready", this.queue.size());

        ResizableArray[] ar;
        try {
            ar = this.queue.take();
        } catch(InterruptedException e) {
            throw new RuntimeException("unexpected interrupt");
        }

        assert ar.length == 4;

        this.random = (ResizableArray.Float) ar[0];
        this.gaussian = (ResizableArray.Double) ar[1];
        this.gaussian_usage = (ResizableArray.Int) ar[2];
        this.spare_gaussian = (ResizableArray.Double) ar[3];

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
            this.spare_gaussian.take(this.deficit);
            log.debug("Reducing deficit of {} from budget {}",
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

        this.gaussian.waste(1);
        this.gaussian_usage.waste(1);
        this.spare_gaussian.waste(1);
        return this.random.take(1);
    }

    @Override
    public double gaussian() {
        if (this.haveGaussian) {
            this.haveGaussian = false;
            return this.spareGaussian;
        }

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
        this.gaussian_usage.waste(usage);
        this.random.waste(usage);
        double ans = this.gaussian.take(usage);
        this.spareGaussian = this.spare_gaussian.take(usage);
        this.haveGaussian = true;

        return ans;
    }
}
