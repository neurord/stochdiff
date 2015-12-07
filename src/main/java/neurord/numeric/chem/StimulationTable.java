package neurord.numeric.chem;

import java.util.ArrayList;
import java.util.List;

import neurord.model.InjectionStim;

public class StimulationTable {

    public static class Stimulation {
        public final int species;
        public final String site;
        public final double rate, onset, duration, period, end;

        public Stimulation(InjectionStim source, int species, int train) {
            final double train_offset, end;
            if (Double.isNaN(source.getPeriod())) {
                train_offset = source.getDuration() + source.getInterTrainInterval();
                end = source.getOnset() + source.getDuration();
            } else {
                train_offset = source.getEnd() - source.getOnset() + source.getInterTrainInterval();
                end = source.getEnd() + train*train_offset;
            }

            this.species = species;
            this.site = source.getInjectionSite();
            this.rate = source.getRate();
            this.onset = source.getOnset() + train*train_offset;
            this.duration = source.getDuration();
            this.period = source.getPeriod();
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

    private final ArrayList<Stimulation> stims;
    private final int nspec;

    public StimulationTable(List<InjectionStim> stims, ReactionTable rtab) {
        this.stims = new ArrayList<>();
        this.nspec = rtab.getSpecies().length;

        if (stims != null)
            for (InjectionStim stim: stims) {
                int species = rtab.getSpecieIndex(stim.getSpecies());
                for (int i = 0; i < stim.getNumTrains(); i++)
                    this.stims.add(new Stimulation(stim, species, i));
            }
    }

    public double[][] getStimsForInterval(double time, double dt) {
        double[][] ret = new double[getNStim()][nspec];
        for (int i = 0; i < ret.length; i++) {
            Stimulation stim = this.stims.get(i);
            double f = stim.effectiveRate(time, dt);

            if (f > 0)
                ret[i][stim.species] = f * stim.rate * dt;
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
