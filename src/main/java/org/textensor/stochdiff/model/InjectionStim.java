//6 22 2007: WK added 'interTrainInterval' and 'numTrains'.
//written by Robert Cannon
package org.textensor.stochdiff.model;

import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;

import javax.xml.bind.annotation.*;

public class InjectionStim {

    @XmlAttribute public String specieID;

    // this should be the ID of a point in the morphology file
    @XmlAttribute public String injectionSite;

    private double rate;

    private double onset;
    private double duration;
    private Double period;
    private Double end;

    private Double interTrainInterval; //the time interval between the end and
    //the start(onset) of two consecutive input trains
    private Integer numTrains;

    public void writeTo(StimulationTable stab, ReactionTable rtab) {
        int specInd = rtab.getSpecieIndex(specieID);
        double[] vrate = new double[rtab.getNSpecies()];
        vrate[specInd] = rate;
        // above allows option of injecting a combination of species

        if (Double.isInfinite(this.getEnd()) && numTrains > 1)
            throw new RuntimeException("end must be specified with numTrains");

        for (int i = 0; i < this.getNumTrains(); i++)
            if (period == null)
                stab.addSquarePulse(injectionSite,
                                    vrate,
                                    onset + i*(duration + this.getInterTrainInterval()),
                                    duration);
            else
                stab.addPeriodicSquarePulse(injectionSite, vrate,
                                            onset + i*((end-onset) + this.getInterTrainInterval()),
                                            duration, period,
                                            end + i*((end-onset) + this.getInterTrainInterval()));
    }

    public double getOnset() {
        return this.onset;
    }

    public double getDuration() {
        return this.duration;
    }

    public double getPeriod() {
        return this.period != null ? this.period : Double.NaN;
    }

    public double getEnd() {
        return this.end != null ? this.end : Double.POSITIVE_INFINITY;
    }

    public int getNumTrains() {
        return this.numTrains != null ? this.numTrains : 1;
    }

    public double getInterTrainInterval() {
        return this.interTrainInterval != null ? this.interTrainInterval : 0;
    }
}
