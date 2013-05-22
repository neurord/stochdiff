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
                                    stimTab, this.getStimulationTargets());
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
        for (int i = 0; i < ri.length; i++)
            for (int p = rp[i]; p > 0; p--)
                ans *= nstart[ri[i]] * PARTICLES_PUVC / vol;

        return ans;
    }
}
