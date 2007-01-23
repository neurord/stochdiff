package org.textensor.stochdiff.numeric.stochastic;

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

    protected final static int BINOMIAL = 0;
    protected final static int POISSON = 1;

    public final static int NMAX_STOCHASTIC = 120;

    public abstract int nGo(int n, double p, double r);


    public static int gaussianStep(int n, double p, double grv) {
        double rngo = p * n;
        int ngo = (int)Math.round(rngo + grv * Math.sqrt(n * p * (1. - p)));
        return ngo;
    }



}
