package org.textensor.stochdiff.model;

import org.textensor.stochdiff.inter.FloatValued;

import javax.xml.bind.annotation.*;

public class PicoSD implements FloatValued {

    @XmlAttribute public String specieID;
    @XmlAttribute public double value;

    public double getPicoMoleSurfaceDensity() {
        return value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double x) {
        value = x;
    }
}
