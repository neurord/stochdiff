package neurord.model;

import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import neurord.numeric.morph.SpinePopulation;

public class SpineAllocation {
    static final Logger log = LogManager.getLogger();

    @XmlAttribute private String id;

    @XmlAttribute private String spineType;
    @XmlAttribute private String region;

    @XmlAttribute private Double lengthDensity;
    @XmlAttribute private Double areaDensity;

    transient private SpineType r_spineType;

    public void resolve(HashMap<String, SpineType> stHM) {
        this.r_spineType = stHM.get(spineType);
    }

    public String getID() {
        return id;
    }

    public SpinePopulation makePopulation() {
        final double density;
        if (this.areaDensity != null)
            density = this.areaDensity;
        else if (this.lengthDensity != null) {
            log.warn("'lengthDensity' is deprecated, use 'areaDensity' instead");
            density = this.lengthDensity;
        } else
            density = 0;

        if (r_spineType != null && density > 0)
            return new SpinePopulation(id, r_spineType.getProfile(), region, density);
        else
            return null;
    }
}
