package org.textensor.stochdiff.model;

import java.util.HashMap;

import javax.xml.bind.annotation.*;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import org.textensor.stochdiff.numeric.morph.SpinePopulation;

public class SpineAllocation {
    static final Logger log = LogManager.getLogger();

    @XmlAttribute public String id;

    @XmlAttribute public String spineType;
    @XmlAttribute public String region;

    @XmlAttribute public double lengthDensity;

    @XmlAttribute public double areaDensity;

    transient private SpineType r_spineType;

    public void resolve(HashMap<String, SpineType> stHM) {
        this.r_spineType = stHM.get(spineType);
    }

    public String getID() {
        return id;
    }

    public SpinePopulation makePopulation() {
        final double density;
        if (areaDensity > 0)
            density = this.areaDensity;
        else if (lengthDensity > 0) {
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
