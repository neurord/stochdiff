package org.textensor.stochdiff;

import org.textensor.stochdiff.numeric.grid.DeterministicGridCalc;
import org.textensor.stochdiff.numeric.grid.SteppedStochasticGridCalc;
import org.textensor.stochdiff.numeric.grid.ExactStochasticGridCalc;
import org.textensor.stochdiff.numeric.pool.*;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.model.SDRun;
import org.textensor.report.E;
import java.lang.reflect.Constructor;

/*
 * This is an enumeration of all the calculation methods. The names
 * are matched from the "calculation" attribute of the top level
 * model specification.
 *
 *  In effect, this just servers to match strings (the calculation field)
 *  to classes, without being too fragile. Eg, you can refactor the
 *  names of the calculation classes themselves withoug breaking
 *  anything.
 */




public enum SDCalcType {
    SMP_EULER(ForwardEulerPoolCalc.class),
    SMP_SEMI_IMPLICIT_EULER(SemiImplicitEulerPoolCalc.class),
    SMP_IMPLICIT_EULER(ImplicitEulerPoolCalc.class),
    SMP_FORWARD_EXPONENTIAL(ForwardExponentialPoolCalc.class),
    SMP_RK4(RungeKutta4PoolCalc.class),
    GRID_STEPPED_CONTINUOUS(DeterministicGridCalc.class),
    GRID_STEPPED_STOCHASTIC(SteppedStochasticGridCalc.class),
    GRID_STEPPED_EXACT(ExactStochasticGridCalc.class);

    private final Class cls;

    SDCalcType(Class cls) {
        this.cls = cls;
    }

    public BaseCalc getCalc(int trial, SDRun sdr) {
        BaseCalc ret = null;
        try {
            Class[] argTyp = {Integer.TYPE, SDRun.class};
            Constructor constructor = this.cls.getConstructor(argTyp);
            Object[] args = {trial, sdr};
            ret = (BaseCalc)(constructor.newInstance(args));

        } catch (Exception ex) {
            E.error("ex " + ex + " cannot instantiate " + name() + " " + this.cls);
            ex.printStackTrace();
        }
        return ret;
    }
}
