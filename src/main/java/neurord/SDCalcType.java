package neurord;

import neurord.numeric.grid.DeterministicGridCalc;
import neurord.numeric.grid.SteppedStochasticGridCalc;
import neurord.numeric.grid.AdaptiveGridCalc;
import neurord.numeric.pool.*;
import neurord.numeric.BaseCalc;
import neurord.model.SDRun;
import java.lang.reflect.Constructor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
    GRID_EXACT(AdaptiveGridCalc.class),
    GRID_ADAPTIVE(AdaptiveGridCalc.class);

    static final Logger log = LogManager.getLogger();

    private final Class cls;

    SDCalcType(Class cls) {
        this.cls = cls;
    }

    public BaseCalc getCalc(int trial, SDRun sdr) {
        try {
            Class[] argTyp = {Integer.TYPE, SDRun.class};
            Constructor constructor = this.cls.getConstructor(argTyp);
            Object[] args = {trial, sdr};
            return (BaseCalc)(constructor.newInstance(args));
        } catch (Exception e) {
            log.error("{}: cannot instantiate {}", this, this.cls.getName(), e);
            throw new RuntimeException(e);
        }
    }
}
