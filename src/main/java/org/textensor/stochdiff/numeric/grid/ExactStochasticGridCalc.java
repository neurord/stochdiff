package org.textensor.stochdiff.numeric.grid;

import java.util.List;
import java.util.Collection;

import org.textensor.stochdiff.model.SDRunWrapper;
import org.textensor.util.ArrayUtil;
import org.textensor.util.inst;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ExactStochasticGridCalc extends StochasticGridCalc {
    static final Logger log = LogManager.getLogger(ExactStochasticGridCalc.class);

    /* Timestamp when queue creation was finished */
    private long real_start_time;
    private long real_end_time;

    NextEventQueue neq;
    ArrayList<IGridCalc.Event> events
        = log_events ? new ArrayList<IGridCalc.Event>() : null;

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
        this.real_start_time = this.real_end_time = System.currentTimeMillis();
    }

    @Override
    public void footer() {
        super.footer();
        log.info("Queue suffered {} swaps", this.neq.queue.swaps);
        log.info("Leapt {} ({} events, {} e/l average), waited {} times",
                 this.neq.leaps, this.neq.leap_extent,
                 ((double)this.neq.leap_extent)/this.neq.leaps,
                 this.neq.normal_waits);

        long time = this.real_end_time - this.real_start_time;
        double speed = 1000*(this.wrapper.sdRun.getEndTime() - this.wrapper.sdRun.getStartTime())/time;
        log.info("Real simulation took {} ms, {} ms/s", time, speed);
    }

    public double advance(double tnow) {
        for(double time = tnow; time < tnow+dt; ) {
            double next = this.neq.advance(time, tnow + dt,
                                           this.reactionEvents,
                                           this.diffusionEvents,
                                           this.stimulationEvents,
                                           this.events);
            assert next >= time: next;
            time = next;
        }

        this.ninjected += ArrayUtil.sum(this.diffusionEvents);
        this.real_end_time = System.currentTimeMillis();
        return dt;
    }

    public static double calculatePropensity(int[] ri, int[] pi,
                                             int[] rs, int[] ps,
                                             int[] rp,
                                             double rate, double vol,
                                             int[] nstart) {
        double ans = rate;
        for (int i = 0; i < ri.length; i++) {
            /* Special case for pseudo-higher-order reactions to make
               sure that the population doesn't go negative. */
            if (nstart[ri[i]] < rs[i])
                return 0;
            ans *= nstart[ri[i]];
            for (int p = 1; p < rp[i]; p++)
                ans *= (nstart[ri[i]] - p) * PARTICLES_PUVC / vol;
        }

        return ans;
    }

    public Collection<IGridCalc.Event> getEvents() {
        Collection<IGridCalc.Event> recent = this.events;
        /* If it was null, it should stay null. Otherwise, reinitalize. */
        if (recent != null)
            this.events = inst.newArrayList();
        return recent;
    }
}
