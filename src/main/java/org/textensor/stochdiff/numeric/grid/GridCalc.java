package org.textensor.stochdiff.numeric.grid;

import java.util.Arrays;

import org.textensor.stochdiff.model.SDRunWrapper;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.math.Column;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.util.ArrayUtil;
import org.textensor.util.Settings;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class GridCalc extends BaseCalc implements IGridCalc {
    static final Logger log = LogManager.getLogger(GridCalc.class);

    final static boolean count_events = Settings.getProperty("stochdiff.count_events", false);

    ReactionTable rtab;

    double dt;

    public int nel, nspec;
    public String[] species;

    double[] volumes;
    double[] fdiff;

    int[][] neighbors;
    double[][] couplingConstants;

    public int[] eltregions;

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

    protected GridCalc(int trial, SDRunWrapper sdm) {
        super(trial, sdm);
    }

    protected void init() {
        stateSaveTime = wrapper.sdRun.getStateSaveInterval();
        if (stateSaveTime <= 0.0) {
            stateSaveTime = 1.e9;
        }

        VolumeGrid grid = this.wrapper.getVolumeGrid();

        nel = grid.getNElements();
        volumes = grid.getElementVolumes();

        rtab = this.wrapper.getReactionTable();
        species = rtab.getSpecies();
        nspec = rtab.getNSpecies();

        neighbors = grid.getPerElementNeighbors();
        couplingConstants = grid.getPerElementCouplingConstants();

        eltregions = grid.getRegionIndexes();

        fdiff = rtab.getDiffusionConstants();

        surfaceAreas = grid.getExposedAreas();

        if (count_events) {
            this.stimulationEvents = new int[nel][nspec];

            this.reactionEvents = new int[nel][rtab.getNReaction()];

            this.diffusionEvents = new int[nel][nspec][];
            for (int iel = 0; iel < nel; iel++)
                for (int k = 0; k < nspec; k++) {
                    int nn = neighbors[iel].length;
                    diffusionEvents[iel][k] = new int[nn];
                }
        }

        this.dt = this.wrapper.stepSize();
    }

    protected double endtime() {
        return this.wrapper.sdRun.getEndTime();
    }

    @Override
    protected void _run() {
        init();

        double time = this.wrapper.sdRun.getStartTime();
        double endtime = this.endtime();

        for(ResultWriter resultWriter: this.resultWriters)
            resultWriter.writeGrid(this.wrapper.getVolumeGrid(),
                                   time, this.wrapper.fnmsOut, this);

        log.info("Trial {}: running from time={} ms to time={} ms", this.trial(), time, endtime);

        long startTime = System.currentTimeMillis();
        double writeTime = time - 1.e-9;

        double[] writeTimeArray = new double[this.wrapper.fnmsOut.length];
        Arrays.fill(writeTimeArray, -1.e-9);

        while (time <= endtime) {

            if (time >= writeTime) {
                log.info("Trial {}: time {} dt={}", this.trial(), time, dt);

                for(ResultWriter resultWriter: this.resultWriters)
                    resultWriter.writeGridConcs(time, nel, this.wrapper.getOutputSpecies(), this);

                writeTime += this.wrapper.sdRun.outputInterval;
                if (count_events) {
                    ArrayUtil.fill(this.stimulationEvents, 0);
                    ArrayUtil.fill(this.diffusionEvents, 0);
                    ArrayUtil.fill(this.reactionEvents, 0);
                }
            }
            for (int i = 0; i < this.wrapper.fnmsOut.length; i++)
                if (time >= writeTimeArray[i]) {
                    for(ResultWriter resultWriter: this.resultWriters)
                        resultWriter.writeGridConcsDumb(i, time, nel, this.wrapper.fnmsOut[i], this);
                    writeTimeArray[i] += Double.valueOf(this.wrapper.dtsOut[i]);
                }

            if (time < endtime) {
                time += advance(time, time + dt);

                if (time >= stateSaveTime) {
                    for(ResultWriter resultWriter: this.resultWriters)
                        resultWriter.saveState(time, this.wrapper.sdRun.stateSavePrefix, this);
                    stateSaveTime += this.wrapper.sdRun.getStateSaveInterval();
                }
            } else
                break;
        }

        if (writeTime < time + this.wrapper.sdRun.outputInterval / 10) {
            log.info("Trial {}: time {} dt={}", this.trial(), time, dt);
            for(ResultWriter resultWriter: this.resultWriters)
                resultWriter.writeGridConcs(time, nel, this.wrapper.getOutputSpecies(), this);
        }
        for (int i = 0; i < this.wrapper.fnmsOut.length; i++)
            if (time >= writeTimeArray[i] + Double.valueOf(this.wrapper.dtsOut[i] / 10))
                for(ResultWriter resultWriter: this.resultWriters)
                    resultWriter.writeGridConcsDumb(i, time, nel, this.wrapper.fnmsOut[i], this);

        log.info("Trial {}: total number of particles at the end: {}",
                 this.trial(), this.getParticleCount());

        for(ResultWriter resultWriter: this.resultWriters)
            resultWriter.closeTrial(this);

        long endTime = System.currentTimeMillis();
        log.info("Trial {}: total run time {} ms", this.trial(), endTime - startTime);

        this.footer();
        this.close();
    }

    protected abstract double advance(double now, double end);

    protected void footer() {}

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
    public int[][] getReactionEvents() {
        return this.reactionEvents;
    }

    @Override
    public int[][][] getDiffusionEvents() {
        return this.diffusionEvents;
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
        if (p == 0)
            return 0;
        else
            return p * intlog(n);
    }
}
