package neurord.numeric.grid;

import neurord.model.SDRun;
import neurord.numeric.math.RandomGenerator;
import neurord.numeric.math.MersenneTwister;
import neurord.numeric.morph.VolumeGrid;
import neurord.util.ArrayUtil;
import neurord.util.Settings;
import neurord.util.Logging;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class StochasticGridCalc extends GridCalc {
    public static final Logger log = LogManager.getLogger();

    final static boolean log_events =
        Settings.getProperty("neurord.log_events",
                             "Log detailed information about every event",
                             false);

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
        assert(this.nel > 0);
        assert(this.nspec > 0);
        this.wkA = new int[this.nel][this.nspec];

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
        final VolumeGrid grid = sdrun.getVolumeGrid();

        /* volume concentrations */
        for (int i = 0; i < this.nel; i++) {
            double v = volumes[i];
            String region = grid.getElementRegion(i);
            double[] rcs = sdrun.getRegionConcentration(region);

            for (int j = 0; j < this.nspec; j++) {
                wkA[i][j] = this.random.round(v * rcs[j] * PARTICLES_PUVC);
                log.debug("el.{} {}: {} × {} × {} → {}",
                          i, species[j],
                          v, rcs[j], PARTICLES_PUVC, wkA[i][j]);
            }

            double[] rcs2 = sdrun.getRegionConcentration(grid.getElementRegion(i));
            if (!Arrays.equals(rcs, rcs2)) {
                log.error("RegionConcentrations are not equal for {} {} {}:\n{}\n{}",
                          i, region, grid.getElementRegion(i),
                          rcs, rcs2);
                throw new RuntimeException("RegionConcentrations are not equal");
            }
        }

        log.debug("volume only:\n{}", wkA);

        /* surface concentrations */
        for (int i = 0; i < this.nel; i++) {
            double a = this.surfaceAreas[i];
            if (a > 0) {
                double[] sds = sdrun.getRegionSurfaceDensity(grid.getElementRegion(i));

                for (int j = 0; j < this.nspec; j++)
                    if (!Double.isNaN(sds[j]))
                        // nan means not specified by the user
                        wkA[i][j] = this.random.round(a * sds[j] * PARTICLES_PUASD);

                double[] sds2 = sdrun.getRegionSurfaceDensity(grid.getElementRegion(i));
                if (!Arrays.equals(sds, sds2)) {
                    log.error("RegionSurfaceDensities are not equal:\n{}\n{}", sds, sds2);
                    throw new RuntimeException("RegionSurfaceDensities are not equal");
                }
            }
        }

        log.debug("with surface:\n{}", wkA);
    }

    @Override
    protected void footer() {
        log.log(Logging.NOTICE,
                "Used up {} random numbers", this.random.used());
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
