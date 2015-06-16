package org.textensor.stochdiff.numeric.pool;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.math.Matrix;
import org.textensor.stochdiff.numeric.math.Column;


/*
 * This solves d/dt (Delta_C) = pC + M Delta_C
 *          Delta_C = pC dt + exp(M dt) Delta_C
 *          (I - exp(M dt)) Delta_C = pC dt
 *
 * For example, in the reaction A + B -> C, we evaluate the change in A using the rate
 * computed from the value of B at the BEGINNING of the step, but the correct
 * value of A  AT EACH INSTANT during the step.
 * Likewise, B depends on the value of A at the beginning but takes account of its own
 * chaning value.
 *
 *  This is particularly nice if the reactions are all independent, (like beta decays or channel state
 *  transitions). Then you get the exact answer for any future time with a single step.
 *  (unless numerical truncation errors become important).
 */



public class ForwardExponentialPoolCalc extends DeterministicPoolCalc {

    public ForwardExponentialPoolCalc(int trial, SDRun sdm) {
        super(trial, sdm);
    }



    public double advance() {
        Column cp = rtab.getProductionColumn(mconc);
        Matrix m = rtab.getIncrementRateMatrix(mconc);

        m = m.expOf(dt);
        Column cpdt = cp.times(dt);

        m.negate();

        Column dc = m.LUSolve(cpdt);

        mconc.decrementBy(dc);

        mconc.print();

        return dt;
    }



    public long getParticleCount() {
        // TODO Auto-generated method stub
        return 0;
    }




}
