package neurord.model;

import java.util.Arrays;
import javax.xml.bind.annotation.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import neurord.inter.FloatValued;

public class PicoSD implements FloatValued {
    static final Logger log = LogManager.getLogger();

    @XmlAttribute public String specieID;
    @XmlAttribute public double value;

    public double getValue() {
        return value;
    }

    public void setValue(double x) {
        value = x;
    }

    public void verify(String[] regions, String[] species) {
        if (!Arrays.asList(species).contains(this.specieID)) {
            log.error("PicoSD specified for species \"{}\", not in {}",
                      this.specieID, species);
            throw new RuntimeException("PicoSD with unknown species: " + this.specieID);
        }
    }
}
