package neurord.numeric.chem;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import neurord.model.InjectionStim;
import neurord.numeric.BaseCalc.distribution_t;

public class StimulationTable {
    static final Logger log = LogManager.getLogger();

    public static class Stimulation {
        public final int species;
        public final String site;
        public final distribution_t distribution;
        public final double rate, onset, duration, period, end;

        public Stimulation(int species,
                           String site,
                           distribution_t distribution,
                           int train,
                           double rate,
                           double onset,
                           double duration,
                           double iti,
                           double period,
                           double end) {
            this.species = species;
            this.site = site;
            this.distribution = distribution;

            final double train_offset, real_end;
            if (Double.isNaN(period)) {
                train_offset = duration + iti;
                real_end = onset + duration;
            } else {
                train_offset = end - onset + iti;
                real_end = end + train*train_offset;
            }

            this.rate = rate;
            this.onset = onset + train*train_offset;
            this.duration = duration;
            this.period = period;
            this.end = real_end;
        }

        @Override
        public String toString() {
            return String.format("%s →%s %s rate=%g onset=%f duration=%f period=%f end=%f",
                                 getClass().getSimpleName(), site, distribution,
                                 rate,
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
                double duration = this.duration;
                if (!Double.isNaN(this.end))
                    duration = Math.min(duration, this.end - pons);
                return pulseOverlap(start, dt, pons, duration);
            }
        }

        private static double pulseOverlap(double t, double dt, double ons, double dur) {
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

        public static void addTo(ArrayList<Stimulation> stims, int species, InjectionStim stim) {
            final double[][] rates = stim.getRates();

            if (rates != null)
                for (int i = 0; i < rates.length; i++) {
                    double time = rates[i][0], rate = rates[i][1];
                    if (rate == 0)
                        continue;

                    final double duration;
                    if (i < rates.length - 1)
                        duration = rates[i+1][0] - time;
                    else
                        duration = Double.POSITIVE_INFINITY;

                    stims.add(new Stimulation(species,
                                              stim.getInjectionSite(),
                                              stim.getDistribution(),
                                              0,
                                              rate,
                                              time,
                                              duration,
                                              0,
                                              Double.NaN,
                                              Double.NaN));
                }
            else
                for (int i = 0; i < stim.getNumTrains(); i++)
                    stims.add(new Stimulation(species,
                                              stim.getInjectionSite(),
                                              stim.getDistribution(),
                                              i,
                                              stim.getRate(),
                                              stim.getOnset(),
                                              stim.getDuration(),
                                              stim.getInterTrainInterval(),
                                              stim.getPeriod(),
                                              stim.getEnd()));
        }
    }

    private final ArrayList<Stimulation> stims;
    private final int nspec;

    public StimulationTable(List<InjectionStim> stims, ReactionTable rtab) {
        this.stims = new ArrayList<>();
        this.nspec = rtab.getSpecies().length;

        if (stims != null)
            for (InjectionStim stim: stims)
                Stimulation.addTo(this.stims,
                                  rtab.getSpecieIndex(stim.getSpecies()),
                                  stim);
    }

    public double[][] getStimsForInterval(double time, double dt) {
        double[][] ret = new double[this.stims.size()][nspec];
        for (int i = 0; i < ret.length; i++) {
            Stimulation stim = this.stims.get(i);
            double f = stim.effectiveRate(time, dt);

            if (f > 0)
                ret[i][stim.species] = f * stim.rate * dt;
        }
        return ret;
    }

    public ArrayList<Stimulation> getStimulations() {
        return this.stims;
    }

    public String[] getTargetIDs() {
        String[] ret = new String[this.stims.size()];
        for (int i = 0; i < ret.length; i++)
            ret[i] = this.stims.get(i).site;
        return ret;
    }
}
