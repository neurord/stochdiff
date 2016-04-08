//3 5 2008: WK changed the initial value of the denominator variable in the extractGrid function from 3 to 1
//6 22 2007: WK modified the extractGrid() function to calculate the side-length of
//           each volume element (which is a square with a predefined thickness).
//6 19 2007: WK added 1 variable and 1 function to be able to output by user-specified 'region's.
//5 16 2007: WK added 4 variables and 5 functions (within <--WK ... WK-->)
//written by Robert Cannon
package neurord.numeric;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.Random;

import neurord.model.*;
import neurord.numeric.chem.ReactionTable;
import neurord.numeric.chem.StimulationTable;
import neurord.numeric.morph.VolumeGrid;
import neurord.numeric.grid.ResultWriter;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Units: concentrations are expressed in nM and volumes in cubic microns
 * So, in these units, one Litre is 10^15 and a 1M solution is 10^9.
 * The conversion factor between concentrations and particle number is
 * therefore
 * nparticles = 6.022^23 * vol/10^15 * conc/10^9
 * ie, nparticles = 0.6022 * vol * conc
 */

public abstract class BaseCalc implements Runnable {
    static final Logger log = LogManager.getLogger();

    // particles Per Unit Volume and Concentration
    public static final double PARTICLES_PUVC = 0.602214179;
    public static final double LN_PARTICLES_PUVC = Math.log(PARTICLES_PUVC);

    // particles per unit area and surface density
    // (is the same as PUVC - sd unit is picomoles per square metre)
    public static final double PARTICLES_PUASD = PARTICLES_PUVC;

    // converting particle numbers to concentrations
    // nanomoles per particle per unit volume
    // ie, each particle added to a cubic micron increases
    // the nanoMolar concentration this much
    public static final double NM_PER_PARTICLE_PUV = 1. / PARTICLES_PUVC;

    protected final ArrayList<ResultWriter> resultWriters = new ArrayList<>();

    public enum distribution_t {
        BINOMIAL,
        POISSON,
        EXACT,
    }

    public enum algorithm_t {
        INDEPENDENT,
        SHARED,
        PARTICLE,
    }

    public enum output_t {
        NUMBER,
        CONCENTRATION,
    }

    protected final distribution_t distID;
    protected final algorithm_t algoID;

    final public boolean writeConcentration;

    private final int trial;
    protected SDRun sdRun;

    public BaseCalc(int trial, SDRun sdRun) {
        this.trial = trial;
        this.sdRun = sdRun;

        this.distID = sdRun.getDistribution();
        this.algoID = sdRun.getAlgorithm();
        this.writeConcentration =
            output_t.valueOf(sdRun.outputQuantity) == output_t.CONCENTRATION;
        log.info("Writing particle numbers as {}s", sdRun.outputQuantity);
    }

    public int trial() {
        return this.trial;
    }

    public SDRun getSource() {
        return this.sdRun;
    }

    private long seed = -1;
    public long getSimulationSeed() {
        if (this.seed == -1) {
            if (this.sdRun.simulationSeed > 0)
                seed = this.sdRun.simulationSeed;
            else
                seed = Math.abs(new Random().nextInt());
            log.info("Trial {}: running with simulationSeed {}", this.trial(), seed);
        }
        return seed;
    }

    public void addResultWriter(ResultWriter rw) {
        rw.init("cctdif2d"); // others....
        this.resultWriters.add(rw);
    }

    protected abstract void _run();

    @Override
    public void run() {
        try {
            this._run();
        } catch(Error e) {
            log.error("{}: failed (seed={})", this, seed);
            throw e;
        }
    }

    public void close() {
        for (ResultWriter resultWriter: this.resultWriters)
            resultWriter.close();
    }

    public abstract long getParticleCount();
}
