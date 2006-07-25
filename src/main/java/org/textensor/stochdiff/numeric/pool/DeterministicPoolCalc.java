package org.textensor.stochdiff.numeric.pool;

import org.textensor.stochdiff.model.SDRun;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.math.Column;


// deterministic single mixed pool;



public abstract class DeterministicPoolCalc extends BaseCalc {


    Column mconc;

    ReactionTable rtab;

    double dt;

    double time;

    public DeterministicPoolCalc(SDRun sdm) {
        super(sdm);
    }


    public final void init() {
        rtab = getReactionTable();

        rtab.print();

        mconc = new Column(getNanoMolarConcentrations());
        dt = sdRun.fixedStepDt;
    }


    public final void run() {
        init();

        mconc.print();

        time = 0.;
        double runtime = sdRun.runtime;

        dpcInit();

        while (time < runtime) {
            time += advance();
        }
    }


    public void dpcInit() {
        // subclasses can override with extra initialization if necessary;
    }


    public abstract double advance();


}
