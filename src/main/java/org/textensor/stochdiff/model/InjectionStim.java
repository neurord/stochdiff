//7 25 2007: WK made the default value of numTrains zero per RO.
//6 22 2007: WK added 'interTrainInterval' and 'numTrains'.
//written by Robert Cannon
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

    //<--WK
    public double interTrainInterval; //the time interval between the end and
    //the start(onset) of two consecutive input trains
    public int numTrains;
    //WK-->

    public void writeTo(StimulationTable stab, ReactionTable rtab) {
        int specInd = rtab.getSpecieIndex(specieID);
        double[] vrate = new double[rtab.getNSpecies()];
        vrate[specInd] = rate;
        // above allows option of injecting a combination of species

        //<--WK wrapped the if-else block within a for-loop to utilize
        //      two new variables, interTrainInterval and numTrains.
        //<--WK 7 25-2007
        //  making the default numTrains be one per RO; this means
        //  that the numTrains value, if specified, in xxx_stim.xml file
        //  must be greater than zero!
        if (numTrains == 0)
        {
            numTrains = 1;
        }
        //WK 7 25 2007--> **** KB 05/03/2011 remove onset from calculation of onset and end for multiple trains
        for (int i = 0; i < numTrains; i++)
        {
            if (period <= 0) {
                stab.addSquarePulse(injectionSite, vrate, onset + i*(duration + interTrainInterval), duration);
            } else {
                stab.addPeriodicSquarePulse(injectionSite, vrate,
                                            onset + i*((end-onset) + interTrainInterval),
                                            duration, period,
                                            end + i*((end-onset) + interTrainInterval));
            }
            //original if-else block
            /*if (period <= 0) {
               stab.addSquarePulse(injectionSite, vrate, onset, duration);
            } else {
               stab.addPeriodicSquarePulse(injectionSite, vrate, onset, duration,
                         period, end);
            }
            */
        }
        //WK-->
    }

}
