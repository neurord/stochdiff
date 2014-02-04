package org.textensor.stochdiff.model;

import javax.xml.bind.annotation.*;

public class SpecieRef {

    @XmlAttribute
    private String specieID;

    @XmlAttribute
    private int n = -1;

    @XmlAttribute
    private int power = 1;

    /**
     * The stochiometry (number of molecules consumed during a reaction).
     * Defaults to 'power' if not specified.
     */
    public int getStochiometry() {
        return n > 0 ? n : power;
    }

    /**
     * The order in reaction (propensity is proportional to the number
     * of molecules raised to this power).
     * Defaults to 1 if not specified.
     */
    public int getPower() {
        return power;
    }

    public String getSpecieID() {
        return specieID;
    }
}
