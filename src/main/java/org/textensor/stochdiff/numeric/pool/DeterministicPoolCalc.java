package org.textensor.stochdiff.numeric.pool;

import org.textensor.stochdiff.model.SDRunWrapper;
import org.textensor.stochdiff.numeric.BaseCalc;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.math.Column;


// deterministic single mixed pool;



public abstract class DeterministicPoolCalc extends BaseCalc {


    Column mconc;

    ReactionTable rtab;

    double dt;

    double time;

    public DeterministicPoolCalc(int trial, SDRunWrapper sdm) {
        super(trial, sdm);
    }


    public final void init() {
        rtab = this.wrapper.getReactionTable();
        rtab.print();

        mconc = new Column(this.wrapper.getRegionConcentrations()[0]);
        dt = this.wrapper.sdRun.getFixedStepDt();
    }

    @Override
    protected void _run() {
        init();

        mconc.print();

        time = this.wrapper.sdRun.getStartTime();
        double endtime = this.wrapper.sdRun.getEndTime();

        dpcInit();

        while (time < endtime)
            time += advance();
    }


    public void dpcInit() {
        // subclasses can override with extra initialization if necessary;
    }


    public abstract double advance();


}
