package org.textensor.stochdiff.numeric.stochastic;

import org.textensor.stochdiff.numeric.math.RandomGenerator;
import static org.textensor.stochdiff.numeric.BaseCalc.distribution_t;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class StepGenerator {
    static final Logger log = LogManager.getLogger();

    protected final distribution_t mode;
    protected final RandomGenerator random;

    public StepGenerator(distribution_t mode, RandomGenerator random) {
        this.mode = mode;
        this.random = random;
    }

    public final static int NMAX_STOCHASTIC = 120;
    public final static int NP = 30;         // AB Changed from 20 to 30. 2011.09.23

    protected abstract int nGo(int n, double lnp, double r);

    /*
     * Return the number of successes in n trials, with probability of success
     * in a single trial p.
     *
     * @param n: number of trials
     * @param p: probabilify of a success in one trial
     * @returns: number of successes
     */
    public int nGo(int n, double lnp) {
        return this.nGo(n, lnp, this.random.random());
    }

    private static int gaussianStep(int n, double p, double grv, double urv) {
        final double rngo = p * n + grv * Math.sqrt(n * p * (1 - p));
        int ngo = (int)rngo;
        if (rngo - ngo > urv)
            ngo += 1;

        return ngo;
    }

    /**
     * Generate a random number from the normal distribution np±√(np(1-p)).
     */
    public int gaussianStep(int n, double p) {
        return gaussianStep(n, p,
                            this.random.gaussian(), this.random.random());
    }

    /**
     * This just uses the poisson variance in combination with a gaussian random.
     * The alternative is to use a real poisson variable with the desired mean, but the
     * cost is substantially greater (ten times or so).
     */
    private static int poissonStep(double np, double grv, double urv) {
        double rngo = np + grv * Math.sqrt(np);
        int ngo = (int)rngo;
        ngo += (rngo - ngo > urv ? 1 : 0);
        if (ngo >= 0)
            return ngo;

        log.warn("poisson step is negative: np={}", np);
        return 0;
    }


    /**
     * Generate a number form the approximate Poisson distribution with
     * average np.
     */
    public int poissonStep(double np) {
        return poissonStep(np, this.random.gaussian(), this.random.random());
    }

    /**
     * Return a random variate from one of the distributions
     * appropriate for first-order reactions (binomial, gaussian,
     * poisson, or something in between, based on how large n is,
     * and also distribution_t mode specified when this object
     * was created.
     */
    public int versatile_ngo(String descr, int n, double p) {
        final int ngo;

        if (n == 1)
            ngo = this.random.random() < p ? 1 : 0;
        else if (n < NMAX_STOCHASTIC) {
            ngo  = this.nGo(n, Math.log(p));

            if (ngo < 0)
                throw new RuntimeException("ngo is negative (" + descr + ")");
        } else
            ngo = this._calculate_ngo(descr, n, p);

        return ngo;
    }

    protected int _calculate_ngo(String where, int n, double p){
        final String msg;
        final int ngo;

        switch(mode) {
        case BINOMIAL:
            if (n * p < NP) {
                ngo = this.random.poisson(n * p);
                msg = "n*p < " + NP;
            } else {
                ngo = this.gaussianStep(n, p);
                msg = "n*p >= " + NP;
            }
            break;
        default:
            ngo = this.poissonStep(n * p);
            msg = "not using binomial";
        }

        if (ngo >= 0)
            return ngo;

        log.warn("{} with {}: ngo is NEGATIVE (ngo={}, n={}, p={})",
                 where, msg, ngo, n, p);
        return 0;
    }
}
