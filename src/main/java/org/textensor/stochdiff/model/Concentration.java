package org.textensor.stochdiff.model;

import org.textensor.stochdiff.inter.FloatValued;


public abstract class Concentration implements FloatValued {

    public String specieID;

    public abstract double getNanoMolarConcentration();

    public abstract String makeXMLLine();

}
