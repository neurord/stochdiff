package neurord.numeric.stochastic;

import neurord.numeric.BaseCalc.distribution_t;

public class FixedPStepGenerator {

    double lnp;
    double p;

    NGoTable[] tables;

    distribution_t mode;

    public FixedPStepGenerator(double lnp0, distribution_t mode) {
        lnp = lnp0;
        this.mode = mode;
        tables = new NGoTable[StepGenerator.NMAX_STOCHASTIC + 1];
        p = Math.exp(lnp);
    }


    public int nGo(int n, double r) {
        int ngo = 0;
        if (n == 0) {
            ngo = 0;

        } else if (n == 1) {
            ngo = (r < p ? 1 : 0);

        } else if (n > StepGenerator.NMAX_STOCHASTIC) {
            // A This case could be caught before we get here
            // B You don't need r for a determiistic calculation
            // C There is probably a case for sampling from a normal for
            // n all the same rather than just picking the mean as we do here?
            ngo = (int)(p * n + 0.5);


        } else {
            // Lazy evaluation - maybe we never need some of the tables
            // (eg if particle counts are always very low)


            if (tables[n] == null) {
                tables[n] = new NGoTable(n, lnp, mode);
            }
            ngo = tables[n].nGo(r);

        }

        return ngo;

    }
}
