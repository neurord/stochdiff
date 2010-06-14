package org.textensor.stochdiff.model;


public class PicoSD extends SurfaceDensity {

    public double value;



    public double getPicoMoleSurfaceDensity() {
        return value;
    }


    public double getValue() {
        return value;
    }

    public void setValue(double x) {
        value = x;
    }

    public String makeXMLLine() {
        return "<PicoSD specieID=\"" + specieID + "\" value=\"" + value + "\"/>";
    }
}
