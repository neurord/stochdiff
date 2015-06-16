package org.textensor.stochdiff.numeric.pool;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.math.Column;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ForwardEulerPoolCalc extends DeterministicPoolCalc {
    static final Logger log = LogManager.getLogger(ForwardEulerPoolCalc.class);

    public ForwardEulerPoolCalc(int trial, SDRun sdm) {
        super(trial, sdm);
    }

    public double advance() {
        Column rates = rtab.getRateColumn(mconc);

        Column prod = rtab.getProductionMatrix().times(rates);

        mconc.incrementBy(prod.times(dt));

        mconc.positivize();

        log.info("advanced fepc {}", time);
        mconc.print();

        return dt;
    }

    public long getParticleCount() {
        // TODO Auto-generated method stub
        return 0;
    }
}
