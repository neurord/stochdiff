package org.textensor.stochdiff.model;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.textensor.stochdiff.inter.FloatValued;

public class ConcentrationSet implements Regional {

    @XmlAttribute public String region;

    @XmlElements({
            @XmlElement(name="NanoMolarity", type=NanoMolarity.class),
            @XmlElement(name="NumberDensity", type=NumberDensity.class)
                })
    public ArrayList<Concentration> concentrations;

    transient HashMap<String, Concentration> concHM;

    public HashMap<String, Concentration> getConcHM() {
        if (concHM == null) {
            concHM = new HashMap<String, Concentration>();
            if (concentrations != null)
                for (Concentration c : concentrations)
                    concHM.put(c.specieID, c);
        }
        return concHM;
    }

    public Double getNanoMolarConcentration(String id) {
        Concentration conc = this.getConcHM().get(id);
        if (conc == null)
            return null;
        else
            return conc.getNanoMolarConcentration();
    }

    public boolean hasRegion() {
        return region != null;
    }

    public String getRegion() {
        return region;
    }

    public void addFloatValued(ArrayList<FloatValued> afv) {
        afv.addAll(concentrations);
    }
}
