package org.textensor.stochdiff.model;

import java.util.HashMap;

import org.textensor.report.E;
import org.textensor.stochdiff.numeric.morph.SpinePopulation;



public class SpineAllocation {

    public String id;

    public String spineType;
    public String region;

    public double lengthDensity;

    public double areaDensity;

    private SpineType r_spineType;


    public void resolve(HashMap<String, SpineType> stHM) {
        if (stHM.containsKey(spineType)) {
            r_spineType = stHM.get(spineType);

        } else {
            E.error("ref to unknown spine type " + spineType);
        }

    }


    public String getID() {
        return id;
    }

    public SpinePopulation makePopulation() {
        SpinePopulation ret = null;

        double density = 0.;
        if (lengthDensity > 0) {
            E.info("TODO - need area factor");
            density = lengthDensity;

        } else if (areaDensity > 0) {
            density = areaDensity;
        }

        if (r_spineType != null && density > 0) {
            ret = new SpinePopulation(id, r_spineType.getProfile(), region, density);
        }
        return ret;

    }


}
