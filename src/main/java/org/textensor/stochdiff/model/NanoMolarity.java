package org.textensor.stochdiff.model;

import javax.xml.bind.annotation.*;

public class NanoMolarity extends Concentration {

    @XmlAttribute public double value;

    public double getNanoMolarConcentration() {
        return value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double x) {
        value = x;
    }

    public String makeXMLLine() {
        return "<NanoMolarity specieID=\"" + specieID + "\" value=\"" + String.format("%.4g", value) + "\"/>";
    }

}
