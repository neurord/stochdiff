package org.textensor.stochdiff.numeric.chem;

import java.util.ArrayList;

import org.textensor.util.inst;

public class StimulationTable {

    public static class Stimulation {
        public final String site;
        public final double[] rates;
        public final double onset, duration, period, end;

        public Stimulation(String site,
                           double[] rates,
                           double onset, double duration, double period, double end) {
            this.site = site;
            this.rates = rates;
            this.onset = onset;
            this.duration = duration;
            this.period = period;
            this.end = end;
        }

        @Override
        public String toString() {
            return String.format("%s →%s onset=%f duration=%f period=%f end=%f",
                                 getClass().getSimpleName(), site,
                                 onset, duration, period, end);
        }
    }

    private final ArrayList<Stimulation> stims = inst.newArrayList();

    public void addSquarePulse(String site, double[] rate, double onset, double duration) {
        this.addPeriodicSquarePulse(site, rate, onset, duration,
                                    Double.NaN, Double.POSITIVE_INFINITY);
    }

    public void addPeriodicSquarePulse(String site, double[] rate, double onset,
                                       double duration, double period, double end) {
        this.stims.add(new Stimulation(site, rate, onset, duration, period, end));
    }


    public double[][] getStimsForInterval(double time, double dt) {
        int nspec = getNStim() > 0 ? this.stims.get(0).rates.length : 0;
        double[][] ret = new double[getNStim()][nspec];
        for (int i = 0; i < ret.length; i++) {
            Stimulation stim = this.stims.get(i);
            double f = effectiveRate(time, dt,
                                     stim.onset, stim.duration, stim.period, stim.end);

            if (f > 0)
                for (int j = 0; j < nspec; j++)
                    ret[i][j] = f * stim.rates[j] * dt;
        }
        return ret;
    }

    private double effectiveRate(double t, double dt, double ons, double dur, double per, double end) {
        if (per < 0)
            return pulseOverlap(t, dt, ons, dur);
        else {
            if (t > end)
                return 0;
            else {
                // just compare with nearest pulse
                // NB, assumes dt smaller than interpulse interval
                int ipulse = (int)((t - ons) / per + 0.5);
                double pons = ons + ipulse * per;
                return pulseOverlap(t, dt, pons, dur);
            }
        }
    }


    private double pulseOverlap(double t, double dt, double ons, double dur) {
        if (t + dt < ons || t > ons + dur)
            return 0;
        else if (t >= ons && t + dt <= ons + dur)
            // fully inside;
            return 1;
        else if (t <= ons && t + dt >= ons + dur)
            // spans whole pulse;
            return dur / dt;
        else if (t <= ons)
            // straddles start
            return (t + dt - ons) / dt;
        else
            // straddles end;
            return (ons + dur - t) / dt;
    }

    public int getNStim() {
        return this.stims.size();
    }

    public ArrayList<Stimulation> getStimulations() {
        return this.stims;
    }

    public String[] getTargetIDs() {
        String[] ret = new String[this.getNStim()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = this.stims.get(i).site;
        return ret;
    }


    /*
     * following is an alternative stimulation approach - could be useful within
     * the main calculation loop?
     *  // update position in each injection profile. If the value for this //
     * step is non-zero, just add them to the correspoinding element of // wkA //
     * Extensions: could need // - distributed injections (shared over multiple
     * volumes) // - solution injections (multiple species from one profile)
     *
     * for (int iin = 0; iin < ninjection; iin++) { double finj =
     * injvals[iin][injpos[iin]]; // if the next step will take us over a step
     * edge in the injection, // need to work scale by the amount of time in each
     * part if (tnow + dt > injsteps[iin][injpos[iin]+1]) { double fns = (tnow +
     * dt - injsteps[iin][injpos[iin]+1]) / dt; injpos[iin] += 1;
     *
     * finj = (1. - fns) * finj + fns + injvals[iin][injpos[iin]];
     *
     * int np = (int)(finj * dt); if (np > 0) { wkA[injelt[iin]][injspec[iin]] +=
     * np; } } }
     */

}
