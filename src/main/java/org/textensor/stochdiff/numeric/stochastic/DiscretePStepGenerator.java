package org.textensor.stochdiff.numeric.stochastic;

import java.util.HashMap;

/*
 * Caching store of generators for cases where only a fixed number
 * of different probabilities occur in the model. This applies to
 * diffusion on a grid where all the elements have the same (or one
 * of a few) volume and coupling constants. Then you get a different p
 * for each diffusible species and for each different coupling constant.
 *
 * The way to use this is to create a DiscretePStepGenerator at the
 * beginning, and then call getGenerator for each connection in the
 * model. Then during the calculation, call generator.nGo(n, r) to
 * get the number of particles that will move in one step.
 *
 *  Generators are cached in the hash map according to an integer
 *  key based on their probability. Effectively, this splits the
 *  space in log(p) between -20 and 0 into about 2E9 separate bins
 *  and treats all p's in a single bin as the same. It avoids any
 *  risk of creating separate generators for probabilities that
 *  only differ by rounding errors.
 *
 *
 */

import org.textensor.stochdiff.numeric.BaseCalc.distribution_t;
import org.textensor.stochdiff.numeric.math.RandomGenerator;

public class DiscretePStepGenerator extends StepGenerator {

    final HashMap<Integer, FixedPStepGenerator> generators = new HashMap<>();
    public final static double lnpmin = -20;

    public DiscretePStepGenerator(distribution_t mode, RandomGenerator random) {
        super(mode, random);
    }

    public FixedPStepGenerator getGenerator(double lnp) {
        final FixedPStepGenerator ret;
        double po = lnp - lnpmin;
        if (po < 0.)
            po = 0.;

        int ipbin = (int)(Integer.MAX_VALUE * (po / (0. - lnpmin)));
        Integer key = new Integer(ipbin);

        if (this.generators.containsKey(key))
            ret = generators.get(key);
        else {
            ret = new FixedPStepGenerator(lnp, this.mode);
            this.generators.put(key, ret);
        }
        return ret;
    }

    // this is here for completeness, but shouldnt be used
    // in real calculations - you should call getGenerator once
    // at the beginning and then use that - it will be much faster.
    @Override
    protected int nGo(int n, double lnp, double r) {
        return getGenerator(lnp).nGo(n, r);
    }
}
