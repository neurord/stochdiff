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

    public final static int NMAX_STOCHASTIC = 100;

    public abstract int nGo(int n, double p, double r);



}
