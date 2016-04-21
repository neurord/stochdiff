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
        this.random = new MersenneTwister(getSimulationSeed());

        // workspace for the calculation
        assert(nel > 0);
        assert(nspec > 0);
        this.wkA = new int[nel][nspec];

        int[][] pop = this.sdRun.getPopulation();
        if (pop != null) {
            log.info("Using preexisting population");
            if (pop.length != this.wkA.length ||
                pop[0].length != this.wkA[0].length) {
                log.error("Preexisting population shape mismatch (nel×nspec {} != {})",
                          ArrayUtil.xJoined(pop.length, pop[0].length),
                          ArrayUtil.xJoined(this.wkA.length, this.wkA[0].length));
                throw new RuntimeException("Preexisting population shape mismatch");
            }
            ArrayUtil.copy(pop, this.wkA);
        } else
            this.initPopulation(this.wkA, this.sdRun);
    }

    protected void initPopulation(int[][] counts, SDRun sdrun) {
        log.debug("Initializing population based on volume and surface concentrations");
        final String[] species = sdrun.getSpecies();

        /* volume concentrations */
        for (int i = 0; i < nel; i++) {
            double v = volumes[i];
            double[] rcs = sdrun.getRegionConcentration(this.eltregions[i]);

            for (int j = 0; j < nspec; j++) {
                wkA[i][j] = this.random.round(v * rcs[j] * PARTICLES_PUVC);
                log.debug("{} el.{}: {} × {} × {} → {}",
                          species[j], i,
                          v, rcs[j], PARTICLES_PUVC, wkA[i][j]);
            }
        }

        log.debug("volume only:\n{}", wkA);

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
                        wkA[i][j] = this.random.round(a * scs[j] * PARTICLES_PUASD);
        }

        log.debug("with surface:\n{}", wkA);
    }

    @Override
    protected void footer() {
        log.info("Used up {} random numbers", this.random.used());
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
