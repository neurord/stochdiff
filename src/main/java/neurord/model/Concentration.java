package neurord.model;

import java.util.Arrays;
import javax.xml.bind.annotation.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import neurord.inter.FloatValued;

public abstract class Concentration implements FloatValued {
    static final Logger log = LogManager.getLogger();

    @XmlAttribute public String specieID;

    public abstract double getNanoMolarConcentration();

    public void verify(String[] regions, String[] species) {
        if (!Arrays.asList(species).contains(this.specieID)) {
            log.error("Concentration specified for species \"{}\", not in {}",
                      this.specieID, species);
            throw new RuntimeException("Concentration with unknown species: " + this.specieID);
        }
    }
}
