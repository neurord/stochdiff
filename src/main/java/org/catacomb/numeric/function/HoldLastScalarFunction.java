package org.catacomb.numeric.function;

import org.catacomb.report.E;


public class HoldLastScalarFunction implements ScalarFunction {

    int ntv;
    double[] times;
    double[] values;

    int itv;


    public HoldLastScalarFunction(double t, double v) {
        times = new double[4];
        values = new double[4];
        times[0] = t;
        values[0] = v;
        ntv = 1;
    }


    public void addTransition(double t, double v) {
        if (t < times[ntv-1]) {
            E.missing("cant add prior transition");
        } else {
            if (ntv == times.length) {
                double[] tt = new double[2 * times.length];
                double[] vv = new double[2 * values.length];
                for (int i = 0; i < ntv; i++) {
                    tt[i] = times[i];
                    vv[i] = values[i];
                }
                times = tt;
                values = vv;
            }

            times[ntv] = t;
            values[ntv] =v;
            ntv += 1;
        }
    }


    public void fixate() {
        addTransition(1.e99, values[ntv-1]);
        // could trim arrays here - EFF
        itv = 0;

        /*
        E.info("fixated hlsf");
        for (int i = 0; i < ntv; i++) {
           E.info("tv point " + times[i] + " " + values[i]);
        }
        */
    }



    public double getScalar(double t) {
        if (t >= times[itv] && t < times[itv+1]) {
            // no change;

        } else if (t < times[itv]) {
            itv = 0;
            while (itv < ntv-1 && t >= times[itv+1]) {
                itv++;
            }

        } else {
            while (itv < ntv-1 && t >= times[itv+1]) {
                itv++;
                //  E.info("HLSF advanced " + itv + " " + values[itv]);
            }
        }

        return values[itv];
    }





}
