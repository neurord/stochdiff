package org.textensor.stochdiff.numeric.grid;

import java.util.Arrays;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.math.Column;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.util.ArrayUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class GridCalc extends BaseCalc implements IGridCalc {
    static final Logger log = LogManager.getLogger(GridCalc.class);

    ReactionTable rtab;

    StimulationTable stimTab;

    double dt;

    public int nel, nspec;
    public String[] specieIDs;

    double[] volumes;
    double[] fdiff;

    int[][] neighbors;
    double[][] couplingConstants;

    public int[] eltregions;
    int[][] stimtargets;

    double[] surfaceAreas;

    double stateSaveTime;


    /** The number of events of each reaction since last writeGridConcs.
     * Shapes is [nel x nreactions]. */
    int reactionEvents[][];
    /** The number of diffused particles since last writeGridConcs.
     * Shape is [nel x nspecies x neighbors]. The third dimension is
     * "rugged". */
    int diffusionEvents[][][];
    /** The number of injected particles since last writeGridConcs.
     * Shape is [nel x nspecies]. */
    int stimulationEvents[][];

    int ninjected = 0;


    protected GridCalc(SDRun sdm) {
        super(sdm);
    }

    protected void init() {
        stateSaveTime = sdRun.getStateSaveInterval();
        if (stateSaveTime <= 0.0) {
            stateSaveTime = 1.e9;
        }

        nel = this.getVolumeGrid().getNElements();
        volumes = this.getVolumeGrid().getElementVolumes();

        rtab = getReactionTable();
        specieIDs = rtab.getSpecieIDs();
        nspec = rtab.getNSpecies();

        neighbors = this.getVolumeGrid().getPerElementNeighbors();
        couplingConstants = this.getVolumeGrid().getPerElementCouplingConstants();

        eltregions = this.getVolumeGrid().getRegionIndexes();

        fdiff = rtab.getDiffusionConstants();

        surfaceAreas = this.getVolumeGrid().getExposedAreas();

        // RO
        // ----------------------
        // System.out.println("Number of files        : " + NspeciesFilef);
        // System.out.println("Total numer of species : " + NspeciesIDsOutf);

        // ----------------------
        // RO
        extractOutputScheme(rtab); // see BaseCalc.java

        stimTab = getStimulationTable();
        stimtargets = this.getVolumeGrid().getAreaIndexes(stimTab.getTargetIDs());

        stimulationEvents = new int[nel][nspec];

        reactionEvents = new int[nel][rtab.getNReaction()];

        diffusionEvents = new int[nel][nspec][];
        for (int iel = 0; iel < nel; iel++)
            for (int k = 0; k < nspec; k++) {
                int nn = neighbors[iel].length;
                diffusionEvents[iel][k] = new int[nn];
            }

        dt = sdRun.fixedStepDt;
    }

    public int run() {
        init();

        double time = sdRun.getStartTime();
        double endtime = sdRun.getEndTime();

        for(ResultWriter resultWriter: this.resultWriters)
            resultWriter.writeGrid(this.getVolumeGrid(), sdRun.getStartTime(), fnmsOut, this);

        log.info("Running from time=" + time + " ms to time=" + endtime + " ms");

        double tlog = 5.;

        long startTime = System.currentTimeMillis();
        double writeTime = time - 1.e-9;

        double[] writeTimeArray = new double[fnmsOut.length];
        Arrays.fill(writeTimeArray, -1.e-9);

        while (time < endtime) {

            if (time >= writeTime) {
                for(ResultWriter resultWriter: this.resultWriters)
                    resultWriter.writeGridConcs(time, nel, ispecout, this);

                writeTime += sdRun.outputInterval;
                ArrayUtil.fill(this.stimulationEvents, 0);
                ArrayUtil.fill(this.diffusionEvents, 0);
                ArrayUtil.fill(this.reactionEvents, 0);
            }
            for (int i = 0; i < fnmsOut.length; i++) {
                if (time >= writeTimeArray[i]) {
                    for(ResultWriter resultWriter: this.resultWriters)
                        resultWriter.writeGridConcsDumb(i, time, nel, fnmsOut[i], this);
                    writeTimeArray[i] += Double.valueOf(dtsOut[i]);
                }
            }

            time += advance(time);

            if (time > tlog) {
                log.info("time {} dt={}", time, dt);
                tlog += Math.max(50 * sdRun.outputInterval, 5);
            }

            if (time >= stateSaveTime) {
                for(ResultWriter resultWriter: this.resultWriters)
                    resultWriter.saveState(time, sdRun.stateSavePrefix, this);
                stateSaveTime += sdRun.getStateSaveInterval();
            }
        }

        log.info("Injected {} particles", ninjected);

        long endTime = System.currentTimeMillis();
        log.info("Total run time {} ms", endTime - startTime);

        return 0;
    }

    protected abstract double advance(double time);

    @Override
    public int getNumberElements() {
        return nel;
    }

    @Override
    public long getParticleCount() {
        long ret = 0;
        for (int i = 0; i < nel; i++)
            for (int j = 0; j < nspec; j++)
                ret += this.getGridPartNumb(i, j);

        return ret;
    }

    @Override
    public String[] getSpecieIDs() {
        return this.specieIDs;
    }

    @Override
    public int[] getEltRegions() {
        return this.eltregions;
    }

    @Override
    public int[][] getReactionEvents() {
        return this.reactionEvents;
    }

    @Override
    public int[][][] getDiffusionEvents() {
        return this.diffusionEvents;
    }

    @Override
    public int[][] getStimulationTargets() {
        return this.stimtargets;
    }

    @Override
    public int[][] getStimulationEvents() {
        return this.stimulationEvents;
    }

    /*
     * Common utilities
     */

    final private static double[] intlogs = ArrayUtil.logArray(10000);
    public final static double intlog(int i) {
        if (i <= 0)
            return intlogs[0];
        else
            return i < intlogs.length ? intlogs[i] : Math.log(i);
    }

    protected static double ln_propensity(int n, int p) {
        return p * intlog(n);
    }
}
