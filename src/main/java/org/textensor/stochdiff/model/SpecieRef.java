package org.textensor.stochdiff.model;

import javax.xml.bind.annotation.*;

public class SpecieRef {

    @XmlAttribute
    private String specieID;

    @XmlAttribute
    private Integer n;

    @XmlAttribute
    private Integer power;

    /**
     * The stochiometry (number of molecules consumed during a reaction).
     * Defaults to 'power' if not specified.
     */
    public int getStochiometry() {
        return n != null ? n : this.getPower();
    }

    /**
     * The order in reaction (propensity is proportional to the number
     * of molecules raised to this power).
     * Defaults to 1 if not specified.
     */
    public int getPower() {
        return power != null ? power : 1;
    }

    public String getSpecieID() {
        return specieID;
    }
}
