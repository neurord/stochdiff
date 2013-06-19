package org.textensor.stochdiff.numeric.grid;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.util.ArrayUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ExactStochasticGridCalc extends StochasticGridCalc {
    static final Logger log = LogManager.getLogger(ExactStochasticGridCalc.class);

    NextEventQueue neq;

    public ExactStochasticGridCalc(SDRun sdm) {
        super(sdm);
    }

    @Override
    public final void init() {
        super.init();

        neq = NextEventQueue.create(this.wkA, this.random,
                                    getVolumeGrid(), rtab,
                                    stimTab, this.getStimulationTargets(),
                                    this.sdRun.tolerance);
    }

    @Override
    public void footer() {
        super.footer();
        log.info("Queue suffered {} swaps", this.neq.queue.swaps);
    }

    public double advance(double tnow) {
        for(double time = tnow; time < tnow+dt; ) {
            double next = neq.advance(time, tnow + dt,
                                      this.reactionEvents,
                                      this.diffusionEvents,
                                      this.stimulationEvents);
            assert next >= time: next;
            time = next;
        }

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
}
