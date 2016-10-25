package neurord.numeric.grid;

import java.util.Arrays;
import java.util.List;

import neurord.model.SDRun;
import neurord.model.IOutputSet;
import neurord.numeric.BaseCalc;
import neurord.numeric.math.Column;
import neurord.numeric.chem.ReactionTable;
import neurord.numeric.chem.StimulationTable;
import neurord.numeric.morph.VolumeGrid;
import neurord.util.ArrayUtil;
import neurord.util.Settings;
import neurord.util.Logging;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public abstract class GridCalc extends BaseCalc implements IGridCalc {
    public static final Logger log = LogManager.getLogger();

    ReactionTable rtab;

    double dt;

    public int nel, nspec;
    public String[] species;

    double[] volumes;
    double[] fdiff;

    int[][] neighbors;

    public int[] eltregions;

    double[] surfaceAreas;

    final double[] dtsOut;

    private static double[] makeDtsOut(List<? extends IOutputSet> outputs, double fallback) {
        int nos = outputs != null ? outputs.size() : 0;
        double[] dtsOut = new double[nos];

        log.info("Extracting dts for {} output files", nos);

        for (int i = 0; i < nos; i++)
            dtsOut[i] = outputs.get(i).getOutputInterval(fallback);

        return dtsOut;
    }

    protected GridCalc(int trial, SDRun sdRun) {
        super(trial, sdRun);

        this.dtsOut = makeDtsOut(this.sdRun.getOutputSets(), this.sdRun.getFixedStepDt());
    }

    protected void init() {
        VolumeGrid grid = this.sdRun.getVolumeGrid();

        nel = grid.size();
        volumes = grid.getElementVolumes();

        rtab = this.sdRun.getReactionTable();
        species = rtab.getSpecies();
        nspec = rtab.getNSpecies();

        neighbors = grid.getPerElementNeighbors();

        eltregions = grid.getRegionIndexes();

        fdiff = rtab.getDiffusionConstants();

        surfaceAreas = grid.getExposedAreas();

        double dt = this.sdRun.stepSize();
        this.dt = Math.min(dt, ArrayUtil.min(this.dtsOut));
    }

    protected double endtime() {
        return this.sdRun.getEndTime();
    }

    @Override
    protected void _run() {
        init();

        double begintime = this.sdRun.getStartTime(), time = begintime;
        double endtime = this.endtime();

        for(ResultWriter resultWriter: this.resultWriters)
            resultWriter.writeGrid(this.sdRun.getVolumeGrid(), time, this);

        log.log(Logging.NOTICE,
                "Trial {}: running from {} to {} ms",
                this.trial(), time, endtime);

        long startTime = System.currentTimeMillis();
        double writeTime = time - 1.e-9;
        final double statInterval = this.sdRun.getStatisticsInterval();
        double statTime = time + statInterval;

        double[] writeTimeArray = new double[this.dtsOut.length];
        Arrays.fill(writeTimeArray, -1.e-9);

        long old_events = 0;
        long old_wall_time = System.currentTimeMillis();

        log.info("main outputInterval={} outputIntervals={}", this.dt, this.dtsOut);

        while (time <= endtime) {

            if (time >= writeTime) {
                long wall_time = System.currentTimeMillis();
                if (wall_time > old_wall_time + 100) {
                    /* avoid printing statistics with very low accuracy */
                    long events = this.eventCount();
                    double speed = (double)(events - old_events)
                        / (wall_time - old_wall_time);
                    log.info("Trial {}: time {} dt={} ({}%) events={} {}/ms",
                             this.trial(), time, dt,
                             (int) ((time - begintime) / (endtime - begintime) * 100),
                             events - old_events, (int) speed);

                    old_events = events;
                    old_wall_time = wall_time;
                } else
                    log.info("Trial {}: time {} dt={}", this.trial(), time, dt);

                for (ResultWriter resultWriter: this.resultWriters)
                    resultWriter.writeOutputInterval(time, this);

                writeTime += this.sdRun.getOutputInterval();

                if (this.sdRun.getStatisticsInterval() > 0)
                    this.resetEventStatistics();
            }
            for (int i = 0; i < this.dtsOut.length; i++)
                if (time >= writeTimeArray[i]) {
                    for (ResultWriter resultWriter: this.resultWriters)
                        resultWriter.writeOutputScheme(i, time, this);
                    writeTimeArray[i] += Double.valueOf(this.dtsOut[i]);
                }

            if (statInterval > 0 && time > statTime) {
                for (ResultWriter resultWriter: this.resultWriters)
                    resultWriter.writeEventStatistics(time, this);
                statTime += statInterval;
            }

            if (time < endtime)
                time += advance(time, time + dt);
            else
                break;
        }

        if (writeTime < time + this.sdRun.getOutputInterval() / 10) {
            log.info("Trial {}: time {} dt={} (100%)", this.trial(), time, dt);
            for (ResultWriter resultWriter: this.resultWriters)
                resultWriter.writeOutputInterval(time, this);
        }
        for (int i = 0; i < this.dtsOut.length; i++)
            if (time >= writeTimeArray[i] + Double.valueOf(this.dtsOut[i] / 10))
                for (ResultWriter resultWriter: this.resultWriters)
                    resultWriter.writeOutputScheme(i, time, this);
        if (statInterval > 0 && time > statTime - statInterval / 2 ||
            statInterval == 0)
            for (ResultWriter resultWriter: this.resultWriters)
                resultWriter.writeEventStatistics(time, this);

        log.info("Trial {}: total number of particles at the end: {}",
                 this.trial(), this.getParticleCount());

        for(ResultWriter resultWriter: this.resultWriters)
            resultWriter.closeTrial(this);

        long endTime = System.currentTimeMillis();
        log.log(Logging.NOTICE,
                "Trial {}: total run time {} ms", this.trial(), endTime - startTime);

        this.footer();
        this.close();
    }

    protected abstract double advance(double now, double end);

    protected void footer() {}

    abstract protected long eventCount();

    protected abstract void resetEventStatistics();

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
}
