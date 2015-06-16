package org.textensor.stochdiff.numeric.pool;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.math.Matrix;
import org.textensor.stochdiff.numeric.math.Column;


/*
 * This is a backward euler in the concentrations, but uses the rates
 * fixed at the start of the step.  If the rates are just
 * spontaneous transitions, then it is the same as a fully implicit
 * backward Euler. Otherwise, it leaves out the fact that the
 * concentration changes of reacting species will have changed
 * the rates for other reactants by the end of the step.
 *
 * For example, in the reaction A + B -> C, we evaluate the change
 * in A as the timestep times the reaction rate using the value of
 * A at the END of the step, and the value of  B at the BEGINNING
 * of the step. Likewise, the value of B is computed using its
 * concentration at the end of the step, and  A at the beginning.
 *
 *  One good thing about this is that the concentrations will never go negative.
 */



public class SemiImplicitEulerPoolCalc extends DeterministicPoolCalc {

    public SemiImplicitEulerPoolCalc(int trial, SDRun sdm) {
        super(trial, sdm);
    }



    public double advance() {
        Matrix m = rtab.getIncrementRateMatrix(mconc);

        Column cp = rtab.getProductionColumn(mconc);

        /*
         * solve   delta_C = cp dt + m delta_C dt
         *         (m dt -I) delta_C = -cp dt
         */


        Column cpdt = cp.times(dt);

        // we don't need m again, so we compute I - m dt in place (ie, overwriting the existing matrix);
        m.multiplyBy(dt);
        m.subtractIdentity();
        m.negate();

        Column dc = m.LUSolve(cpdt);

        mconc.incrementBy(dc);

        mconc.print();

        return dt;
    }



    public long getParticleCount() {
        // TODO Auto-generated method stub
        return 0;
    }




}
