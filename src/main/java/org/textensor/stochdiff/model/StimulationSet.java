package org.textensor.stochdiff.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.numeric.chem.ReactionTable;
import org.textensor.stochdiff.numeric.chem.StimulationTable;
import org.textensor.util.inst;

public class StimulationSet {

    @XmlElement(name="InjectionStim")
    public ArrayList<InjectionStim> stimProcesses;

    public StimulationTable makeStimulationTable(ReactionTable rtab) {
        StimulationTable stab = new StimulationTable();
        for (InjectionStim istim : stimProcesses)
            istim.writeTo(stab, rtab);

        return stab;
    }
}
