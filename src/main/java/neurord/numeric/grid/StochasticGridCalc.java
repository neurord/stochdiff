package neurord.numeric.grid;

import neurord.model.SDRun;
import neurord.numeric.math.RandomGenerator;
import neurord.numeric.math.MersenneTwister;
import neurord.util.ArrayUtil;
import neurord.util.Settings;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class StochasticGridCalc extends GridCalc {
    static final Logger log = LogManager.getLogger();

    final static boolean log_events = Settings.getProperty("neurord.log_events", false);

    RandomGenerator random;

    int[][] wkA;

    public StochasticGridCalc(int trial, SDRun sdm) {
        super(trial, sdm);
    }

    @Override
    public void init() {
        super.init();

        // something to generate the random nunmbers
        random = new MersenneTwister(getSimulationSeed());

        // workspace for the calculation
        assert(nel > 0);
        assert(nspec > 0);
        wkA = new int[nel][nspec];

        /* volume concentrations */
        for (int i = 0; i < nel; i++) {
            double v = volumes[i];
            double[] rcs = this.sdRun.getRegionConcentrations()[eltregions[i]];

            for (int j = 0; j < nspec; j++)
                wkA[i][j] = this.randomRound(v * rcs[j] * PARTICLES_PUVC);
        }

        /* surface concentrations */
        double[][] regsd = this.sdRun.getRegionSurfaceDensities();

        // apply initial conditions over the grid
        for (int i = 0; i < nel; i++) {
            double a = this.surfaceAreas[i];
            double[] scs = regsd[this.eltregions[i]];
            if (a > 0 && scs != null)
                for (int j = 0; j < nspec; j++)
                    if (!Double.isNaN(scs[j]))
                        // nan means not specified by the user;
                        wkA[i][j] = this.randomRound(a * scs[j] * PARTICLES_PUASD);
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
