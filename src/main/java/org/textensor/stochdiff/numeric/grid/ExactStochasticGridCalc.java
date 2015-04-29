package org.textensor.stochdiff.numeric.grid;

import java.util.List;
import java.util.Collection;

import org.textensor.stochdiff.model.SDRunWrapper;
import org.textensor.util.Settings;
import org.textensor.util.ArrayUtil;
import org.textensor.util.inst;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ExactStochasticGridCalc extends StochasticGridCalc {
    static final Logger log = LogManager.getLogger(ExactStochasticGridCalc.class);

    final static boolean curtail_leaps = Settings.getProperty("stochdiff.curtail_leaps", false);

    /* Timestamp when queue creation was finished */
    private long real_start_time;

    NextEventQueue neq;
    ArrayList<IGridCalc.Happening> events
        = log_events ? new ArrayList<IGridCalc.Happening>() : null;

    public ExactStochasticGridCalc(int trial, SDRunWrapper sdm) {
        super(trial, sdm);
    }

    @Override
    public final void init() {
        super.init();

        this.neq = NextEventQueue.create(this.wkA, this.random, null,
                                         this.wrapper.getVolumeGrid(), rtab,
                                         this.wrapper.getStimulationTable(),
                                         this.wrapper.getStimulationTargets(),
                                         this.wrapper.sdRun.tolerance,
                                         this.wrapper.sdRun.leap_min_jump,
                                         this.trial() == 0);
        this.real_start_time = System.currentTimeMillis();
    }

    @Override
    public void footer() {
        super.footer();
        log.info("Queue suffered {} swaps", this.neq.queue.swaps);
        log.info("Accuracy control parameter ε={}", this.neq.tolerance);
        log.info("Leapt {} ({} events, {} e/l average), waited {} times",
                 this.neq.leaps, this.neq.leap_extent,
                 ((double)this.neq.leap_extent)/this.neq.leaps,
                 this.neq.normal_waits);

        long time = System.currentTimeMillis() - this.real_start_time;
        double speed = 1000*(this.wrapper.sdRun.getEndTime() - this.wrapper.sdRun.getStartTime())/time;
        log.info("Real simulation took {} ms, {} ms/s", time, speed);
    }

    @Override
    public double advance(double tnow, double tend) {
        double endtime = this.endtime();

        for(double time = tnow; time < tend; ) {
            double next = this.neq.advance(time, tend,
                                           curtail_leaps ? tend : endtime,
                                           this.reactionEvents,
                                           this.diffusionEvents,
                                           this.stimulationEvents,
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
    public Collection<IGridCalc.Event> getEvents() {
        return this.neq.getEvents();
    }

    @Override
    public Collection<IGridCalc.Happening> getHappenings() {
        Collection<IGridCalc.Happening> recent = this.events;
        /* If it was null, it should stay null. Otherwise, reinitalize. */
        if (recent != null)
            this.events = inst.newArrayList();
        return recent;
    }
}
