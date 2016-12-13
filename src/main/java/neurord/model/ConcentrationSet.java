package neurord.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import neurord.inter.FloatValued;

public class ConcentrationSet implements Regional {
    static final Logger log = LogManager.getLogger();

    @XmlAttribute public String region;

    @XmlElements({
            @XmlElement(name="NanoMolarity", type=NanoMolarity.class),
            @XmlElement(name="NumberDensity", type=NumberDensity.class)
                })
    public ArrayList<Concentration> concentrations;

    transient HashMap<String, Concentration> concHM;
    private synchronized HashMap<String, Concentration> getConcHM() {
        if (concHM == null) {
            concHM = new HashMap<>();
            if (concentrations != null)
                for (Concentration c: concentrations)
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

    public void verify(String[] regions, String[] species) {
        if (this.hasRegion() && !Arrays.asList(regions).contains(this.region)) {
            log.error("ConcentrationSet has region \"{}\", not in {}",
                      this.region, regions);
            throw new RuntimeException("ConcentrationSet with bad region: " + this.region);
        }
        for (Concentration conc: this.concentrations)
            conc.verify(regions, species);
    }
}
