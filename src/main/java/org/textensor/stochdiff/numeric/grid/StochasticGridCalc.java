package org.textensor.stochdiff.numeric.grid;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.math.RandomGenerator;
import org.textensor.stochdiff.numeric.math.MersenneTwister;
import org.textensor.util.ArrayUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class StochasticGridCalc extends GridCalc {
    static final Logger log = LogManager.getLogger(StochasticGridCalc.class);

    RandomGenerator random;

    int[][] wkA;

    public StochasticGridCalc(SDRun sdm) {
        super(sdm);
    }

    @Override
    public void init() {
        super.init();

        // something to generate the random nunmbers
        random = new MersenneTwister(getCalculationSeed());

        // workspace for the calculation
        assert(nel > 0);
        assert(nspec > 0);
        wkA = new int[nel][nspec];

        for (int i = 0; i < nel; i++) {
            double v = volumes[i];
            double[] rcs = getRegionConcentrations()[eltregions[i]];

            for (int j = 0; j < nspec; j++)
                wkA[i][j] = this.randomRound(v * rcs[j] * PARTICLES_PUVC);
        }
    }

    @Override
    protected void footer() {
        log.info("Used up {} random numbers", this.random.used());
    }

    protected int randomRound(double number) {
        int i = (int) number;
        double d = number - i;

        // random allocation to implement the remainder
        // (some cells get an extra particle, some don't)
        if (random.random() < d)
            i++;

        return i;
    }

    @Override
    public boolean preferConcs(){ return false; }

    @Override
    public int getGridPartNumb(int i, int j) {
        return wkA[i][j];
    }

    @Override
    public double getGridPartConc(int i, int j) {
        int val = getGridPartNumb(i, j);
        return val * NM_PER_PARTICLE_PUV / volumes[i];
    }
}
