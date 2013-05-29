//6 22 2007: WK added 'interTrainInterval' and 'numTrains'.
//written by Robert Cannon
package org.textensor.stochdiff.model;

import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;


public class InjectionStim {

    public String specieID;

    // this should be the ID of a point in the morphology file
    public String injectionSite;

    public double rate;

    public double onset;
    public double duration;
    public double period;
    public double end = Double.POSITIVE_INFINITY;

    public double interTrainInterval; //the time interval between the end and
    //the start(onset) of two consecutive input trains
    public int numTrains = 1;

    public void writeTo(StimulationTable stab, ReactionTable rtab) {
        int specInd = rtab.getSpecieIndex(specieID);
        double[] vrate = new double[rtab.getNSpecies()];
        vrate[specInd] = rate;
        // above allows option of injecting a combination of species

        if (Double.isInfinite(this.end) && numTrains > 1)
            throw new RuntimeException("end must be specified with numTrains");

        for (int i = 0; i < numTrains; i++)
            if (period <= 0)
                stab.addSquarePulse(injectionSite,
                                    vrate,
                                    onset + i*(duration + interTrainInterval),
                                    duration);
            else
                stab.addPeriodicSquarePulse(injectionSite, vrate,
                                            onset + i*((end-onset) + interTrainInterval),
                                            duration, period,
                                            end + i*((end-onset) + interTrainInterval));
    }
}
