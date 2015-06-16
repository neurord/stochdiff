package org.textensor.stochdiff.numeric.pool;

import org.textensor.stochdiff.model.SDRun;


/*
 * Fourth order Runge Kutta.
 *
 * Left as an exercise for the reader.
 */



public class RungeKutta4PoolCalc extends DeterministicPoolCalc {

    public RungeKutta4PoolCalc(int trial, SDRun sdm) {
        super(trial, sdm);
    }



    public double advance() {


        // so something in here;

        return dt;
    }



    public long getParticleCount() {
        // TODO Auto-generated method stub
        return 0;
    }




}
