package org.textensor.stochdiff.numeric.grid;

import java.util.List;
import java.util.Collection;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.util.ArrayUtil;

import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ExactStochasticGridCalc extends StochasticGridCalc {
    static final Logger log = LogManager.getLogger(ExactStochasticGridCalc.class);

    NextEventQueue neq;
    final ArrayList<IGridCalc.Event> events
        = log_events ? new ArrayList<IGridCalc.Event>() : null;

    public ExactStochasticGridCalc(SDRun sdm) {
        super(sdm);
    }

    @Override
    public final void init() {
        super.init();

        this.neq = NextEventQueue.create(this.wkA, this.random, null,
                                         getVolumeGrid(), rtab,
                                         stimTab, this.getStimulationTargets(),
                                         this.sdRun.tolerance,
                                         this.sdRun.leap_min_jump);
    }

    @Override
    public void footer() {
        super.footer();
        log.info("Queue suffered {} swaps", this.neq.queue.swaps);
        log.info("Leapt {} ({} events), waited {} times",
                 this.neq.leaps, this.neq.leap_extent,
                 this.neq.normal_waits);
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

    public IGridCalc.Event[] getEvents() {
        IGridCalc.Event[] recent = this.events.toArray(new IGridCalc.Event[0]);
        this.events.clear();
        return recent;
    }
}
