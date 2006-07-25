package org.textensor.stochdiff.model;

import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;


public class InjectionStim {

    public String specieID;

    // this should be the ID of a point in the morphology file
    public String injectionSite;

    public double onset;
    public double duration;
    public double rate;

    public double period;

    public double end;



    public void writeTo(StimulationTable stab, ReactionTable rtab) {
        int specInd = rtab.getSpecieIndex(specieID);
        double[] vrate = new double[rtab.getNSpecies()];
        vrate[specInd] = rate;
        // above allows option of injecting a combination of species

        if (period <= 0) {
            stab.addSquarePulse(injectionSite, vrate, onset, duration);
        } else {
            stab.addPeriodicSquarePulse(injectionSite, vrate, onset, duration,
                                        period, end);
        }
    }

}
