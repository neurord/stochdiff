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

        public double effectiveRate(double start, double dt) {
            if (Double.isNaN(this.period))
                return pulseOverlap(start, dt, this.onset, this.duration);
            else {
                // just compare with nearest pulse
                // NB, assumes dt smaller than interpulse interval
                int ipulse = (int)((start - this.onset) / this.period + 0.5);
                double pons = this.onset + ipulse * this.period;
                return pulseOverlap(start, dt, pons, this.duration);
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
    }

    private final ArrayList<Stimulation> stims = inst.newArrayList();

    public void addSquarePulse(String site, double[] rate, double onset, double duration) {
        this.addPeriodicSquarePulse(site, rate, onset, duration,
                                    Double.NaN, onset + duration);
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
            double f = stim.effectiveRate(time, dt);

            if (f > 0)
                for (int j = 0; j < nspec; j++)
                    ret[i][j] = f * stim.rates[j] * dt;
        }
        return ret;
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
}
