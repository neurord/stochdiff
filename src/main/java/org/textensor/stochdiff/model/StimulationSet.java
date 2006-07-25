package org.textensor.stochdiff.model;

import java.util.ArrayList;

import org.textensor.stochdiff.inter.AddableTo;
import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;

public class StimulationSet implements AddableTo {

    public ArrayList<InjectionStim> stimProcesses;


    public StimulationSet() {
        stimProcesses = new ArrayList<InjectionStim>();
    }



    public void add(Object obj) {
        stimProcesses.add((InjectionStim)obj);
    }



    public StimulationTable makeStimulationTable(ReactionTable rtab) {
        StimulationTable stab = new StimulationTable();
        for (InjectionStim istim : stimProcesses) {
            istim.writeTo(stab, rtab);
        }
        return stab;
    }


}
