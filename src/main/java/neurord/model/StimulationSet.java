package neurord.model;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import neurord.numeric.chem.ReactionTable;
import neurord.numeric.chem.StimulationTable;

public class StimulationSet {

    @XmlElement(name="InjectionStim")
    public List<InjectionStim> stimProcesses = new ArrayList<>();

    public StimulationTable makeStimulationTable(ReactionTable rtab) {
        StimulationTable stab = new StimulationTable();
        for (InjectionStim istim : stimProcesses)
            istim.writeTo(stab, rtab);

        return stab;
    }
}
