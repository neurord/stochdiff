package neurord.numeric.grid;

import java.util.List;
import java.util.Collection;

import neurord.SDCalcType;
import neurord.model.SDRun;
import neurord.util.Settings;
import neurord.util.ArrayUtil;
import neurord.util.Logging;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class AdaptiveGridCalc extends StochasticGridCalc {
    public static final Logger log = LogManager.getLogger();

    final static boolean curtail_leaps = Settings.getProperty("neurord.curtail_leaps",
                                                              "Do not allow leaps to extend past reporting time",
                                                              false);

    /* Timestamp when queue creation was finished */
    private long real_start_time;

    NextEventQueue neq;
    ArrayList<IGridCalc.Happening> events
        = log_events ? new ArrayList<IGridCalc.Happening>() : null;

    int eventStatistics[][];

    public AdaptiveGridCalc(int trial, SDRun sdm) {
        super(trial, sdm);
    }

    @Override
    public final void init() {
        super.init();

        final SDCalcType calculationType = SDCalcType.valueOf(this.sdRun.calculation);
        assert calculationType == SDCalcType.GRID_EXACT ||
               calculationType == SDCalcType.GRID_ADAPTIVE;
        final boolean adaptive = calculationType == SDCalcType.GRID_ADAPTIVE;
        final String statistics = this.sdRun.getStatistics();

        this.neq = NextEventQueue.create(this.wkA, this.random, null,
                                         this.sdRun.getVolumeGrid(), rtab,
                                         this.sdRun.getStimulationTable(),
                                         adaptive,
                                         this.sdRun.tolerance,
                                         this.sdRun.leap_min_jump,
                                         this.trial() == 0,
                                         statistics);

        final int stat_count = this.neq.stat_count(this.sdRun.getStatistics());
        if (stat_count >= 0)
            this.eventStatistics = new int[stat_count][2];

        this.real_start_time = System.currentTimeMillis();
    }

    @Override
    public void footer() {
        super.footer();
        log.info("Queue suffered {} swaps", this.neq.queue.swaps);
        log.log(Logging.NOTICE,
                "Accuracy control parameter ε={}", this.neq.tolerance);
        log.log(Logging.NOTICE,
                "Leapt {} ({} events, {} e/l average), waited {} times",
                 this.neq.leap_extent, this.neq.leaps,
                 (double)this.neq.leap_extent / this.neq.leaps,
                 this.neq.normal_waits);

        long time = System.currentTimeMillis() - this.real_start_time;
        double speed = 1000*(this.sdRun.getEndTime() - this.sdRun.getStartTime())/time;
        log.log(Logging.NOTICE,
                "Real simulation took {} ms, {} ms/s", time, speed);
    }

    @Override
    protected long eventCount() {
        return this.neq.leaps + this.neq.normal_waits;
    }

    @Override
    public double advance(double tnow, double tend) {
        double endtime = this.endtime();

        for(double time = tnow; time < tend; ) {
            double next = this.neq.advance(time, tend,
                                           curtail_leaps ? tend : endtime,
                                           this.eventStatistics,
                                           this.events);
            assert next >= time: next;
            time = next;
        }

        /* If next > tend, it will not be actually executed, so
         * the real end time is tend. */
        return tend - tnow;
    }

    public static double calculatePropensity(int[] ri, int[] pi,
                                             int[] rs, int[] ps,
                                             int[] rp,
                                             double rate, double vol,
                                             int[] nstart) {
        double ans = rate * vol;

        for (int i = 0; i < ri.length; i++) {
            /* Special case for pseudo-higher-order reactions to make
               sure that the population doesn't go negative.
               Stoichiometry is only used for this check. */
            if (nstart[ri[i]] < rs[i])
                return 0;

            /* Reactants must have non-zero stoichiometry, but can have nil power.
               Reactants with nil power do not appear in the propensity formula, except
               for the stoichiometry check above. */
            for (int p = 0; p < rp[i]; p++)
                ans *= (nstart[ri[i]] - p) * NM_PER_PARTICLE_PUV / vol;
        }

        return ans;
    }

    @Override
    public int[][] getEventStatistics() {
        return this.eventStatistics;
    }

    @Override
    protected void resetEventStatistics() {
        ArrayUtil.fill(this.eventStatistics, 0);
    }

    @Override
    public Collection<IGridCalc.Event> getEvents() {
        return this.neq.getEvents();
    }

    @Override
    public Collection<IGridCalc.Happening> getHappenings() {
        Collection<IGridCalc.Happening> recent = this.events;
        /* If it was null, it should stay null. Otherwise, reinitalize. */
        if (recent != null)
            this.events = new ArrayList<>();
        return recent;
    }
}
