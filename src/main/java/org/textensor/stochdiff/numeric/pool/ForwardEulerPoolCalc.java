package org.textensor.stochdiff.numeric.pool;

import org.textensor.report.E;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.math.Column;


public class ForwardEulerPoolCalc extends DeterministicPoolCalc {

    public ForwardEulerPoolCalc(SDRun sdm) {
        super(sdm);
    }



    public double advance() {
        Column rates = rtab.getRateColumn(mconc);

        Column prod = rtab.getProductionMatrix().times(rates);

        mconc.incrementBy(prod.times(dt));

        mconc.positivize();

        E.info(" advanced fepc " + time);
        mconc.print();

        return dt;
    }



    public long getParticleCount() {
        // TODO Auto-generated method stub
        return 0;
    }




}
