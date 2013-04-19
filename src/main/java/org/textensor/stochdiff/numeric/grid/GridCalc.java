package org.textensor.stochdiff.numeric.grid;

import java.util.Arrays;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.stochdiff.numeric.math.Column;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.util.ArrayUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class GridCalc extends BaseCalc implements IGridCalc {
    static final Logger log = LogManager.getLogger(GridCalc.class);

    ReactionTable rtab;
    public VolumeGrid vgrid;

    StimulationTable stimTab;

    double dt;

    public int nel;
    int nspec;
    public String[] specieIDs;

    double[] volumes;
    double[] lnvolumes;
    double[] fdiff;

    public boolean[] submembranes;
    public String[] regionLabels;

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

        rtab = getReactionTable();
        specieIDs = rtab.getSpecieIDs();

        vgrid = getVolumeGrid();
        nel = vgrid.getNElements();

        // WK 6 18 2007
        submembranes = vgrid.getSubmembranes();
        regionLabels = vgrid.getRegionLabels();
        // WK

        eltregions = vgrid.getRegionIndexes();
    }

    public int run() {
        init();

        double time = sdRun.getStartTime();
        double endtime = sdRun.getEndTime();

        for(ResultWriter resultWriter: this.resultWriters)
            resultWriter.writeGrid(vgrid, sdRun.getStartTime(), fnmsOut, this);

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
    public String[] getRegionLabels() {
        return this.regionLabels;
    }

    @Override
    public int[] getEltRegions() {
        return this.eltregions;
    }

    @Override
    public boolean[] getSubmembranes() {
        return this.submembranes;
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
}
