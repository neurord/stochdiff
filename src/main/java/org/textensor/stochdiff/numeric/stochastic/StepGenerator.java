package org.textensor.stochdiff.numeric.stochastic;

import org.textensor.stochdiff.numeric.math.RandomGenerator;

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

    protected final RandomGenerator random;

    public StepGenerator(RandomGenerator random) {
        this.random = random;
    }

    public final static int NMAX_STOCHASTIC = 120;

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
        return ngo + (rngo - ngo > urv ? 1 : 0);
    }

    public int poissonStep(double np) {
        return poissonStep(np, this.random.gaussian(), this.random.random());
    }
}
