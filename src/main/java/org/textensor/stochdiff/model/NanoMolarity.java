package org.textensor.stochdiff.model;


public class NanoMolarity extends Concentration {

    public double value;


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
