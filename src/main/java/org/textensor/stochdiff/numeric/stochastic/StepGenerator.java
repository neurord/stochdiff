package org.textensor.stochdiff.numeric.stochastic;

import org.textensor.stochdiff.numeric.math.RandomGenerator;
import static org.textensor.stochdiff.numeric.BaseCalc.distribution_t;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/*
 * Return the integer number of particles that going to move (or
 * reactions to occur) as a function of:
 * the probability, p, that one will move/occur
 * the number, n,  of particles of the given type in the volume element
 * a uniform random number r
 *
 * This is an abstract class - it just defines the method. Particular
 * instances evaluate the function in different ways.
 */

public abstract class StepGenerator {
    static final Logger log = LogManager.getLogger(StepGenerator.class);

    protected final distribution_t mode;
    protected final RandomGenerator random;

    public StepGenerator(distribution_t mode, RandomGenerator random) {
        this.mode = mode;
        this.random = random;
    }

    public final static int NMAX_STOCHASTIC = 120;
    public final static int NP = 30;         // AB Changed from 20 to 30. 2011.09.23

    protected abstract int nGo(int n, double p, double r);

    public int nGo(int n, double p) {
        return this.nGo(n, p, this.random.random());
    }

    private static int gaussianStep(int n, double p, double grv, double urv) {
        double rngo = (p * n + grv * Math.sqrt(n * p * (1. - p)));
        int ngo = (int)rngo;
        if (rngo - ngo > urv) {
            ngo += 1;
        }
        return ngo;
    }

    public int gaussianStep(int n, double p) {
        return gaussianStep(n, p,
                            this.random.gaussian(), this.random.random());
    }

    //<--WK
    // based on RC's email on 5-17-2007
    //<--RO & WK
    // changed gaussianStep to accept another argument ",
    // int np)"; before it was hardcoded as 10 (on 7 11 2008)
    //RO & WK-->
    private static int gaussianStep(int n, double p, double grv, double urv, double prv, int np)
    {
        double rngo = 0.0;

        if (n*p < np)
        {
            rngo = prv;
        }
        else
        {
            rngo = (p * n + grv * Math.sqrt(n * p * (1. - p)));
        }

        int ngo = (int)rngo;
        if (rngo - ngo > urv) {
            ngo += 1;
        }
        return ngo;
    }
    //WK-->

    public int gaussianStep(int n, double p, int np) {
        double
            g = this.random.gaussian(),
            r = this.random.random(),
            u = this.random.poisson(n * p);

        return gaussianStep(n, p,
                            g, r, u,
                            np);
    }


    /**
     * This just uses the poisson variance in combination with a gaussian random.
     * The alternative is to use a real poisson variable with the desired mean, but the
     * cost is substantially greater (ten times or so).
     */
    private static int poissonStep(double np, double grv, double urv) {
        double rngo = np + grv * Math.sqrt(np); //WK: removed Math.round per RC's email on 5-17-2007
        int ngo = (int)rngo;
        ngo += (rngo - ngo > urv ? 1 : 0);
        if (ngo >= 0)
            return ngo;

        log.warn("poisson step is negative: np={}", np);
        return 0;
    }

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
                ngo = this.gaussianStep(n, p, NP);
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
