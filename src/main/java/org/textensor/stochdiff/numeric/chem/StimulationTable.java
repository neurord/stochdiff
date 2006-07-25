package org.textensor.stochdiff.numeric.chem;


public class StimulationTable {

    int nstim;
    int nspec;

    String[] sites;

    double[][] rates;

    double[] onset;
    double[] duration;
    double[] period;
    double[] ends;


    public StimulationTable() {
        sites = new String[10];
        rates = new double[10][];

        onset = new double[10];
        duration = new double[10];
        period = new double[10];
        ends = new double[10];
    }


    public void addSquarePulse(String injectionSite, double[] vrate,
                               double xonset, double xduration) {
        sites[nstim] = injectionSite;
        rates[nstim] = vrate;
        onset[nstim] = xonset;
        duration[nstim] = xduration;
        period[nstim] = -1;
        nstim++;
        if (nspec <= 0) {
            nspec = vrate.length;
        }
    }


    public void addPeriodicSquarePulse(String injectionSite, double[] vrate,
                                       double xonset, double xduration, double xperiod, double xend) {
        sites[nstim] = injectionSite;
        sites[nstim] = injectionSite;
        rates[nstim] = vrate;
        onset[nstim] = xonset;
        duration[nstim] = xduration;
        period[nstim] = xperiod;
        ends[nstim] = xend;
        nstim++;
        if (nspec <= 0) {
            nspec = vrate.length;
        }
    }


    public double[][] getStimsForInterval(double time, double dt) {
        double[][] ret = new double[nstim][nspec];
        for (int i = 0; i < nstim; i++) {
            double f = effectiveRate(time, dt, onset[i], duration[i], period[i], ends[i]);

            if (f > 0.) {
                for (int j = 0; j < nspec; j++) {
                    ret[i][j] = f * rates[i][j];
                }
            }
        }
        return ret;
    }




    private double effectiveRate(double t, double dt,
                                 double ons, double dur, double per, double end) {
        double f = 0.;
        if (per < 0) {
            f = pulseOverlap(t, dt, ons, dur);
        } else {
            if (t > end) {
                f = 0;
            } else {
                // just compare with nearest pulse
                // NB, assumes dt smaller than interpulse interval
                int ipulse = (int)((t - ons) / per + 0.5);
                double pons = ons + ipulse * per;
                f = pulseOverlap(t, dt, pons, dur);
            }
        }
        return f;
    }

    private double pulseOverlap(double t, double dt, double ons, double dur) {
        double f = 0.;
        if (t + dt < ons || t > ons + dur) {
            f = 0;
        } else if (t > ons && t +dt < ons + dur) {
            // fully inside;
            f = 1;
        } else if (t < ons && t + dt > ons + dur) {
            // spans whole pulse;
            f = dur / dt;
        } else if (t < ons) {
            // straddles start
            f = (t + dt - ons) / dt;
        } else {
            // straddles end;
            f = (ons + dur - t) / dt;
        }
        return f;
    }


    public int getStimIndex(String s) {
        int iret = -1;
        for (int i = 0; i < nstim; i++) {
            if (sites[i].equals(s)) {
                iret = i;
                break;
            }
        }
        return iret;
    }


    public String[] getStimLabels() {
        return getTargetIDs();
    }


    public int getNStim() {
        return nstim;
    }


    public String[] getTargetIDs() {
        String[] ret = new String[nstim];
        for (int i = 0; i < nstim; i++) {
            ret[i] = sites[i];
        }
        return ret;
    }


    /*
     * following is an alternative stimulation approach - could
     * be useful within the main calculation loop?
     *
    // update position in each injection profile. If the value for this
    // step is non-zero, just add them to the correspoinding element of
    // wkA
    // Extensions: could need
    //   - distributed injections (shared over multiple volumes)
    //   - solution injections (multiple species from one profile)

    for (int iin = 0; iin < ninjection; iin++) {
       double finj = injvals[iin][injpos[iin]];
       // if the next step will take us over a step edge in the injection,
       // need to work scale by the amount of time in each part
       if (tnow + dt > injsteps[iin][injpos[iin]+1]) {
          double fns = (tnow + dt - injsteps[iin][injpos[iin]+1]) / dt;
          injpos[iin] += 1;

          finj = (1. - fns) * finj + fns + injvals[iin][injpos[iin]];

          int np = (int)(finj * dt);
          if (np > 0) {
             wkA[injelt[iin]][injspec[iin]] += np;
          }
       }
    }
    */

}
